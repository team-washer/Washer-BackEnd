package com.washer.Things.domain.reservation.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.washer.Things.domain.fcmToken.repository.FcmTokenRepository;
import com.washer.Things.domain.fcmToken.service.FcmService;
import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.machine.repository.MachineRepository;
import com.washer.Things.domain.reservation.entity.Reservation;
import com.washer.Things.domain.reservation.presentation.dto.response.MachineState;
import com.washer.Things.domain.reservation.presentation.dto.response.ReservationHistoryResponse;
import com.washer.Things.domain.reservation.repository.ReservationRepository;
import com.washer.Things.domain.reservation.service.ReservationService;
import com.washer.Things.domain.smartThingsToken.entity.SmartThingsToken;
import com.washer.Things.domain.smartThingsToken.service.SmartThingsTokenService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.global.auditLog.Auditable;
import com.washer.Things.global.exception.HttpException;
import com.washer.Things.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final UserService userService;
    private final ReservationRepository reservationRepository;
    private final MachineRepository machineRepository;
    private final SmartThingsTokenService smartThingsTokenService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final FcmService fcmService;


    @Scheduled(fixedRate = 2000)
    @Transactional
    public void turnOffIdleMachines() {
        List<Machine> machines = machineRepository.findAll();
        SmartThingsToken token = smartThingsTokenService.getToken();

        for (Machine machine : machines) {
            List<Reservation> activeReservations = reservationRepository.findByMachineIdAndStatusInWithLock(
                    machine.getId(),
                    List.of(
                            Reservation.ReservationStatus.confirmed,
                            Reservation.ReservationStatus.running
                    )
            );

            if (activeReservations.isEmpty()) {
                try {
                    turnOffMachine(machine.getDeviceId(), token.getAccessToken());
                } catch (Exception e) {
                    return;
                }
            }
        }
    }
    private void turnOffMachine(String deviceId, String accessToken) {
        String url = "https://api.smartthings.com/v1/devices/" + deviceId + "/commands";

        String body = """
        {
          "commands": [
            {
              "component": "main",
              "capability": "switch",
              "command": "off"
            }
          ]
        }
        """;

        webClient.post()
                .uri(url)
                .headers(h -> h.setBearerAuth(accessToken))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("기기 {} 종료 시도 응답: {}", deviceId);
    }
    @Transactional
    @Auditable(action = "CREATE", resourceType = "Reservation")
    public void createReservation(Long machineId) {

        User user = userService.getCurrentUser();

        if (user.getRestrictedUntil() != null && user.getRestrictedUntil().isAfter(LocalDateTime.now())) {
            throw new HttpException(HttpStatus.FORBIDDEN, "현재 정지된 사용자입니다. 예약이 불가능합니다.");
        }

        List<Reservation> existingReservations = reservationRepository.findActiveByRoomWithLock(
                user.getRoom().getId(),
                List.of(
                        Reservation.ReservationStatus.waiting,
                        Reservation.ReservationStatus.reserved,
                        Reservation.ReservationStatus.confirmed,
                        Reservation.ReservationStatus.running
                )
        );

        if (!existingReservations.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "호실당 한 대의 기기만 예약할 수 있습니다.");
        }

        Machine machine = machineRepository.findWithLockById(machineId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "기기를 찾을 수 없습니다."));

        if (machine.isOutOfOrder()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "해당 기기는 현재 고장 상태입니다.");
        }

        List<Reservation> activeReservations = reservationRepository.findByMachineIdAndStatusInWithLock(
                machineId,
                List.of(
                        Reservation.ReservationStatus.reserved,
                        Reservation.ReservationStatus.confirmed,
                        Reservation.ReservationStatus.running
                )
        );

        Reservation.ReservationStatus initialStatus = activeReservations.isEmpty() ?
                Reservation.ReservationStatus.reserved : Reservation.ReservationStatus.waiting;

        Reservation reservation = Reservation.builder()
                .room(user.getRoom())
                .user(user)
                .machine(machine)
                .type(machine.getType())
                .status(initialStatus)
                .startTime(LocalDateTime.now().plusMinutes(5))
                .build();

        reservationRepository.save(reservation);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> expiredReservations = reservationRepository.findAllByStatusAndStartTimeBefore(
                Reservation.ReservationStatus.reserved,
                now
        );

        for (Reservation r : expiredReservations) {
            reservationRepository.findByIdWithLock(r.getId()).ifPresent(lockedReservation -> {
                if (lockedReservation.getStatus() == Reservation.ReservationStatus.reserved) {
                    lockedReservation.setStatus(Reservation.ReservationStatus.cancelled);
                    lockedReservation.setCancelledAt(now);
                    fcmService.sendToRoom(
                            List.of(lockedReservation.getUser()),
                            "예약 취소",
                            "예약이 자동으로 취소되었어요."
                    );
                }
            });
        }
    }

    @Transactional
    @Auditable(action = "UPDATE", resourceType = "Reservation")
    public void confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."));

        if (reservation.getStatus() != Reservation.ReservationStatus.reserved) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "예약 상태가 시작 가능 상태가 아닙니다.");
        }

        reservation.setStatus(Reservation.ReservationStatus.confirmed);
        reservation.setConfirmedAt(LocalDateTime.now());
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void checkExpiredConfirmedReservations() {
        LocalDateTime now = LocalDateTime.now();
        SmartThingsToken token = smartThingsTokenService.getToken();
        List<Reservation> confirmedReservations = reservationRepository.findAllByStatus(Reservation.ReservationStatus.confirmed);
        for (Reservation r : confirmedReservations) {
            reservationRepository.findByIdWithLock(r.getId()).ifPresent(lockedReservation -> {
                String deviceId = lockedReservation.getMachine().getDeviceId();
                boolean isRunning = isMachineRunning(deviceId, token.getAccessToken(), lockedReservation.getMachine().getType());
                if (isRunning) {
                    if (lockedReservation.getStatus() != Reservation.ReservationStatus.running) {
                        lockedReservation.setStatus(Reservation.ReservationStatus.running);
                    }
                } else if (lockedReservation.getConfirmedAt().plusMinutes(2).isBefore(now)) {
                    lockedReservation.setStatus(Reservation.ReservationStatus.cancelled);
                    lockedReservation.setCancelledAt(now);
                    fcmService.sendToRoom(
                            List.of(lockedReservation.getUser()),
                            "예약 취소",
                            "기기 사용이 시작되지 않아 예약이 취소되었어요."
                    );
                }
            });
        }
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void checkCompletedReservations() {
        LocalDateTime now = LocalDateTime.now();
        SmartThingsToken token = smartThingsTokenService.getToken();

        List<Reservation> runningReservations = reservationRepository.findAllByStatus(Reservation.ReservationStatus.running);

        for (Reservation r : runningReservations) {
            reservationRepository.findByIdWithLock(r.getId()).ifPresent(lockedReservation -> {
                String deviceId = lockedReservation.getMachine().getDeviceId();
                boolean isRunning = isMachineCompleted(deviceId, token.getAccessToken(), lockedReservation.getMachine().getType());

                if (isRunning) {
                    lockedReservation.setStatus(Reservation.ReservationStatus.completed);
                    lockedReservation.setCompletedAt(now);
                    fcmService.sendToRoom(
                            List.of(lockedReservation.getUser()),
                            "세탁 완료",
                            "세탁이 완료되었어요! 기기에서 꺼내 주세요."
                    );
                    promoteNextWaitingReservation(lockedReservation.getMachine().getId(), now);
                }
            });
        }
    }


    @Transactional
    @Auditable(action = "DELETE", resourceType = "Reservation")
    public void cancelReservation(Long reservationId) {
        User currentUser = userService.getCurrentUser();

        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."));

        if (!reservation.getRoom().getId().equals(currentUser.getRoom().getId())) {
            throw new HttpException(HttpStatus.FORBIDDEN, "해당 예약을 취소할 권한이 없습니다.");
        }

        if (reservation.getStatus() == Reservation.ReservationStatus.cancelled ||
                reservation.getStatus() == Reservation.ReservationStatus.completed) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "이미 취소되었거나 완료된 예약입니다.");
        }

        reservation.setStatus(Reservation.ReservationStatus.cancelled);
        reservation.setCancelledAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        promoteNextWaitingReservation(reservation.getMachine().getId(), LocalDateTime.now());
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void checkPausedReservations() {
        LocalDateTime now = LocalDateTime.now();
        SmartThingsToken token = smartThingsTokenService.getToken();

        List<Reservation> runningReservations = reservationRepository.findAllByStatus(Reservation.ReservationStatus.running);

        for (Reservation r : runningReservations) {
            reservationRepository.findByIdWithLock(r.getId()).ifPresent(lockedReservation -> {
                String deviceId = lockedReservation.getMachine().getDeviceId();
                Machine.MachineType type = lockedReservation.getMachine().getType();

                String state = getMachineState(deviceId, token.getAccessToken(), type);

                if ("paused".equalsIgnoreCase(state) || "ready".equalsIgnoreCase(state) || "stop".equalsIgnoreCase(state)) {
                        lockedReservation.setPausedSince(now);
                        lockedReservation.setStatus(Reservation.ReservationStatus.reserved);
                        lockedReservation.setStartTime(LocalDateTime.now().plusMinutes(5));
                        fcmService.sendToRoom(
                                List.of(lockedReservation.getUser()),
                                "기기 정지 감지",
                                "기기가 정지되었습니다 기기를 확인해주세요."
                        );
                } else {
                    lockedReservation.setPausedSince(null);
                }
            });
        }
    }

    @Auditable(action = "UPDATE", resourceType = "Reservation")
    public void promoteNextWaitingReservation(Long machineId, LocalDateTime now) {
        Reservation nextWaiting = reservationRepository
                .findFirstWaitingWithLock(machineId, Reservation.ReservationStatus.waiting)
                .orElse(null);

        if (nextWaiting != null) {
            nextWaiting.setStatus(Reservation.ReservationStatus.reserved);
            nextWaiting.setStartTime(now.plusMinutes(5));
            fcmService.sendToRoom(
                    List.of(nextWaiting.getUser()),
                    "기기 예약 확정",
                    "기기 예약이 확정되었어요! 5분 안에 시작해주세요."
            );
        }
    }

    private boolean isMachineRunning(String deviceId, String accessToken, Machine.MachineType type) {
        MachineState state = fetchMachineStates(deviceId, accessToken, type);
        return "running".equalsIgnoreCase(state.operatingState());
    }

    private boolean isMachineCompleted(String deviceId, String accessToken, Machine.MachineType type) {
        MachineState state = fetchMachineStates(deviceId, accessToken, type);
        return "finished".equalsIgnoreCase(state.jobState()) || "finish".equalsIgnoreCase(state.jobState());
    }

    private String getMachineState(String deviceId, String accessToken, Machine.MachineType type) {
        return fetchMachineStates(deviceId, accessToken, type).operatingState();
    }

    private MachineState fetchMachineStates(String deviceId, String accessToken, Machine.MachineType type) {
        String url = "https://api.smartthings.com/v1/devices/" + deviceId + "/status";

        try {
            String response = webClient.get()
                    .uri(url)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode component = root.path("components").path("main");

            String operatingState;
            String jobState;

            if (type == Machine.MachineType.washer) {
                JsonNode washerState = component.path("samsungce.washerOperatingState");
                operatingState = washerState.path("operatingState").path("value").asText("unknown");
                jobState = washerState.path("washerJobState").path("value").asText("unknown");
            } else {
                JsonNode dryerState = component.path("samsungce.dryerOperatingState");
                operatingState = dryerState.path("operatingState").path("value").asText("unknown");
                jobState = dryerState.path("dryerJobState").path("value").asText("unknown");
            }

            log.info("[기기 상태 조회] deviceId={}, type={}, operatingState={}, jobState={}", deviceId, type, operatingState, jobState);
            return new MachineState(operatingState, jobState);

        } catch (Exception e) {
            log.error("[기기 상태 조회 실패] deviceId={}, error={}", deviceId, e.getMessage(), e);
            return new MachineState("unknown", "unknown");
        }
    }

    @Transactional
    public List<ReservationHistoryResponse> getReservationHistory(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "기기를 찾을 수 없습니다."));

        List<Reservation> recentReservations = reservationRepository
                .findTop5ByMachineIdOrderByCreatedAtDesc(machineId);

        return recentReservations.stream()
                .map(r -> ReservationHistoryResponse.builder()
                        .status(r.getStatus().name().toLowerCase())
                        .createdAt(r.getCreatedAt())
                        .pausedSince(r.getPausedSince())
                        .confirmedAt(r.getConfirmedAt())
                        .startedAt(r.getStartedAt())
                        .completedAt(r.getCompletedAt())
                        .cancelledAt(r.getCancelledAt())
                        .machineLabel(machine.getName())
                        .build())
                .toList();
    }
}
