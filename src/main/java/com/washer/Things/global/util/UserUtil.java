package com.washer.Things.global.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.washer.Things.domain.user.presentation.dto.request.RestrictUserRequest;
import com.washer.Things.domain.user.presentation.dto.response.UserResponse;
import com.washer.Things.domain.user.presentation.dto.response.AdminUserInfoResponse;
import com.washer.Things.domain.reservation.entity.Reservation;
import com.washer.Things.domain.reservation.repository.ReservationRepository;
import com.washer.Things.domain.smartThingsToken.entity.SmartThingsToken;
import com.washer.Things.domain.smartThingsToken.service.SmartThingsTokenService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.entity.enums.Gender;
import com.washer.Things.domain.user.repository.UserRepository;
import com.washer.Things.global.exception.HttpException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserUtil {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final SmartThingsTokenService smartThingsTokenService;
    private final WebClient webClient;

    @Transactional
    public UserResponse getUserInfo() {
        User user = getCurrentUser();

        List<Reservation.ReservationStatus> activeStatuses = List.of(
                Reservation.ReservationStatus.waiting,
                Reservation.ReservationStatus.reserved,
                Reservation.ReservationStatus.confirmed,
                Reservation.ReservationStatus.running
        );

        Optional<Reservation> reservationOpt = reservationRepository.findFirstByRoomIdAndStatusInOrderByCreatedAtAsc(
                user.getRoom().getId(), activeStatuses);

        if (reservationOpt.isEmpty()) {
            return buildUserResponseWithoutReservation(user);
        }

        Reservation reservation = reservationOpt.get();
        String remainingTime = calculateRemainingTimeText(reservation);

        return UserResponse.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .schoolNumber(user.getSchoolNumber())
                .roomNumber(user.getRoom().getName())
                .gender(user.getGender().name().toLowerCase())
                .restrictedUntil(user.getRestrictedUntil() != null ? user.getRestrictedUntil().toString() : null)
                .restrictionReason(user.getRestrictionReason())
                .reservationId(reservation.getId())
                .machineLabel(reservation.getMachine().getName())
                .status(reservation.getStatus().name().toLowerCase())
                .startTime(reservation.getCreatedAt())
                .remainingTime(remainingTime)
                .build();
    }

    private String calculateRemainingTimeText(Reservation reservation) {
        switch (reservation.getStatus()) {
            case reserved:
                return calculateTimeLeftText(reservation.getStartTime());
            case confirmed:
                return calculateTimeLeftText(reservation.getConfirmedAt().plusMinutes(2));
            case running:
                return fetchRemainingTimeFromSmartThingsAsText(reservation);
            default:
                return null;
        }
    }

    private String calculateTimeLeftText(LocalDateTime deadline) {
        Duration duration = Duration.between(LocalDateTime.now(), deadline);
        if (duration.isNegative()) return "00:00:00";

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String fetchRemainingTimeFromSmartThingsAsText(Reservation reservation) {
        String deviceId = reservation.getMachine().getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) return null;

        SmartThingsToken token = smartThingsTokenService.getToken();

        try {
            JsonNode status = webClient.get()
                    .uri("https://api.smartthings.com/v1/devices/{deviceId}/status", deviceId)
                    .headers(headers -> headers.setBearerAuth(token.getAccessToken()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (status == null) return null;

            JsonNode main = status.path("components").path("main");

            String completionTimeUtc = main.path("dryerOperatingState").path("completionTime").path("value").asText(null);
            if (completionTimeUtc == null || completionTimeUtc.equals("none")) {
                completionTimeUtc = main.path("washerOperatingState").path("completionTime").path("value").asText(null);
            }

            if (completionTimeUtc == null || completionTimeUtc.equals("none")) return null;

            ZonedDateTime utcTime = ZonedDateTime.parse(completionTimeUtc);
            LocalDateTime localCompletionTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();

            return calculateTimeLeftText(localCompletionTime);
        } catch (Exception e) {
            return null;
        }
    }

    private UserResponse buildUserResponseWithoutReservation(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .schoolNumber(user.getSchoolNumber())
                .roomNumber(user.getRoom().getName())
                .gender(user.getGender().name().toLowerCase())
                .restrictedUntil(user.getRestrictedUntil() != null ? user.getRestrictedUntil().toString() : null)
                .restrictionReason(user.getRestrictionReason())
                .reservationId(null)
                .machineLabel(null)
                .status(null)
                .startTime(null)
                .remainingTime(null)
                .build();
    }

    @Transactional
    public List<AdminUserInfoResponse> getUsers(String name, Gender gender, String floor) {
        List<User> users = userRepository.findByOptionalFilters(
                (name == null || name.isBlank()) ? null : name,
                gender,
                (floor == null || floor.isBlank()) ? null : floor
        );

        LocalDateTime now = LocalDateTime.now();

        return users.stream()
                .peek(user -> {
                    if (user.getRestrictedUntil() != null && user.getRestrictedUntil().isBefore(now)) {
                        user.setRestrictedUntil(null);
                        user.setRestrictionReason(null);
                    }
                })
                .map(user -> AdminUserInfoResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .schoolNumber(user.getSchoolNumber())
                        .gender(String.valueOf(user.getGender()))
                        .roomName(user.getRoom() != null ? user.getRoom().getName() : null)
                        .restrictedUntil(Optional.ofNullable(user.getRestrictedUntil())
                                .map(Object::toString)
                                .orElse(null))
                        .restrictionReason(user.getRestrictionReason())
                        .build())
                .toList();
    }


    @Transactional
    public void restrictUser(Long userId, RestrictUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        LocalDateTime until = calculateRestrictionEnd(request.getPeriod());
        user.setRestrictedUntil(until);
        user.setRestrictionReason(request.getPestrictionReason());
    }

    private LocalDateTime calculateRestrictionEnd(String period) {
        LocalDateTime now = LocalDateTime.now();

        return switch (period) {
            case "1시간" -> now.plusHours(1);
            case "1일" -> now.plusDays(1);
            case "7일" -> now.plusDays(7);
            default -> throw new HttpException(HttpStatus.BAD_REQUEST, "잘못된 정지 기간입니다.");
        };
    }

    @Transactional
    public void unrestrictUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        user.setRestrictedUntil(null);
        user.setRestrictionReason(null);
    }

    @Transactional
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."));
    }
}
