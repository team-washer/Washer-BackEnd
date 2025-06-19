package com.washer.Things.domain.reservation.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.reservation.entity.Reservation;
import com.washer.Things.domain.reservation.presentation.dto.response.AdminReservationResponse;
import com.washer.Things.domain.reservation.repository.ReservationRepository;
import com.washer.Things.domain.reservation.service.ReservationAdminService;
import com.washer.Things.domain.smartThingsToken.entity.SmartThingsToken;
import com.washer.Things.domain.smartThingsToken.service.SmartThingsTokenService;
import com.washer.Things.global.exception.HttpException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationAdminServiceImpl implements ReservationAdminService {
    private final ReservationRepository reservationRepository;
    private final SmartThingsTokenService smartThingsTokenService;
    private final ReservationServiceImpl reservationService;
    private final WebClient webClient;

    @Transactional
    public List<AdminReservationResponse> getAllActiveReservations(Machine.MachineType type, Machine.Floor floor) {
        List<Reservation.ReservationStatus> activeStatuses = List.of(
                Reservation.ReservationStatus.waiting,
                Reservation.ReservationStatus.reserved,
                Reservation.ReservationStatus.confirmed,
                Reservation.ReservationStatus.running
        );

        List<Reservation> reservations = reservationRepository
                .findByStatusesAndOptionalMachineFilters(activeStatuses, type, floor);

        return reservations.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AdminReservationResponse mapToResponse(Reservation reservation) {
        return AdminReservationResponse.builder()
                .reservationId(reservation.getId())
                .machineLabel(reservation.getMachine().getName())
                .status(reservation.getStatus().name().toLowerCase())
                .startTime(reservation.getCreatedAt())
                .remainingTime(calculateRemainingTimeText(reservation))
                .build();
    }

    private String calculateRemainingTimeText(Reservation reservation) {
        return switch (reservation.getStatus()) {
            case reserved -> formatRemainingTime(reservation.getStartTime());
            case confirmed -> formatRemainingTime(reservation.getConfirmedAt().plusMinutes(2));
            case running -> fetchRemainingTimeFromSmartThings(reservation);
            default -> "00:00:00";
        };
    }

    private String formatRemainingTime(LocalDateTime untilTime) {
        Duration duration = Duration.between(LocalDateTime.now(), untilTime);
        if (duration.isNegative()) return "00:00:00";

        return String.format("%02d:%02d:%02d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart());
    }

    private String fetchRemainingTimeFromSmartThings(Reservation reservation) {
        String deviceId = reservation.getMachine().getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) return "00:00:00";

        SmartThingsToken token = smartThingsTokenService.getToken();

        try {
            JsonNode status = webClient.get()
                    .uri("https://api.smartthings.com/v1/devices/{deviceId}/status", deviceId)
                    .headers(headers -> headers.setBearerAuth(token.getAccessToken()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (status == null) return "00:00:00";

            JsonNode main = status.path("components").path("main");

            String timeText = extractCompletionTime(main, "dryerOperatingState");
            if (timeText == null) {
                timeText = extractCompletionTime(main, "washerOperatingState");
            }

            if (timeText == null || timeText.equals("none")) return "00:00:00";

            ZonedDateTime utcTime = ZonedDateTime.parse(timeText);
            LocalDateTime localTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();

            return formatRemainingTime(localTime);

        } catch (Exception e) {
            return "00:00:00";
        }
    }

    private String extractCompletionTime(JsonNode main, String stateKey) {
        return main.path(stateKey).path("completionTime").path("value").asText(null);
    }

    @Transactional
    public void cancelReservationByAdmin(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."));

        if (reservation.getStatus() == Reservation.ReservationStatus.cancelled ||
                reservation.getStatus() == Reservation.ReservationStatus.completed) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "이미 취소되었거나 완료된 예약입니다.");
        }

        reservation.setStatus(Reservation.ReservationStatus.cancelled);
        reservation.setCancelledAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        reservationService.promoteNextWaitingReservation(reservation.getMachine().getId(), LocalDateTime.now());
    }
}
