package com.washer.Things.domain.machine.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.machine.presentation.dto.response.DeviceInfoResponse;
import com.washer.Things.domain.machine.repository.MachineRepository;
import com.washer.Things.domain.machine.service.MachineInfoService;
import com.washer.Things.domain.reservation.entity.Reservation;
import com.washer.Things.domain.smartThingsToken.entity.SmartThingsToken;
import com.washer.Things.domain.smartThingsToken.service.SmartThingsTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MachineInfoServiceImpl implements MachineInfoService {
    private final SmartThingsTokenService smartThingsTokenService;
    private final MachineRepository machineRepository;
    private final WebClient webClient;

    @Transactional
    public Map<String, List<DeviceInfoResponse>> getMyDevices(String filterType, String filterFloor) {
        smartThingsTokenService.refreshTokenIfNeeded();
        SmartThingsToken token = smartThingsTokenService.getToken();

        String rawResponse = webClient.get()
                .uri("https://api.smartthings.com/v1/devices")
                .headers(headers -> headers.setBearerAuth(token.getAccessToken()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode items = mapper.readTree(rawResponse).path("items");

            Map<String, List<DeviceInfoResponse>> result = new HashMap<>();

            for (JsonNode device : items) {
                String label = device.path("label").asText();
                String deviceId = device.path("deviceId").asText();
                String type = getDeviceType(label);
                String floor = extractFloorFromLabel(label);

                if ((filterType != null && !type.equals(filterType)) ||
                        (filterFloor != null && !floor.equalsIgnoreCase(filterFloor))) {
                    continue;
                }

                JsonNode status = getDeviceStatus(token, deviceId);
                JsonNode main = status.path("components").path("main");

                DeviceInfoResponse.DeviceInfoResponseBuilder builder = DeviceInfoResponse.builder()
                        .label(label)
                        .floor(floor)
                        .powerState(getSafeValue(main, "switch", "switch"))
                        .remainingTime(getRemainingTime(main, type));

                if ("washer".equals(type)) {
                    builder.machineState(getSafeValue(main, "washerOperatingState", "machineState"));
                    builder.jobState(getSafeValue(main, "washerOperatingState", "washerJobState"));
                } else {
                    builder.machineState(getSafeValue(main, "dryerOperatingState", "machineState"));
                    builder.jobState(getSafeValue(main, "dryerOperatingState", "dryerJobState"));
                }

                enrichFromEntity(builder, label, type, floor);

                DeviceInfoResponse deviceInfo = builder.build();
                result.computeIfAbsent(type, k -> new ArrayList<>()).add(deviceInfo);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("디바이스 정보 처리 실패", e);
        }
    }

    private void enrichFromEntity(DeviceInfoResponse.DeviceInfoResponseBuilder builder, String label, String typeStr, String floorStr) {
        Machine.MachineType type = "washer".equals(typeStr) ? Machine.MachineType.washer : Machine.MachineType.dryer;
        Machine.Floor floor = Machine.Floor.valueOf("_" + floorStr);

        machineRepository.findByTypeAndFloorAndLabel(type, floor, label)
                .ifPresent(machine -> {
                    builder.id(machine.getId());
                    builder.isOutOfOrder(machine.isOutOfOrder());

                    List<Map<String, Object>> reservationList = new ArrayList<>();

                    for (Reservation res : machine.getReservations()) {
                        if (EnumSet.of(Reservation.ReservationStatus.waiting,
                                Reservation.ReservationStatus.reserved,
                                Reservation.ReservationStatus.confirmed,
                                Reservation.ReservationStatus.running).contains(res.getStatus())) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("id", res.getId());
                            map.put("room", res.getRoom().getName());
                            map.put("status", res.getStatus());
                            map.put("startTime", res.getCreatedAt());

                            reservationList.add(map);

                            // ✅ remainingTime 덮어쓰기
                            String remaining = calculateRemainingTimeByStatus(res);
                            builder.remainingTime(remaining);
                        }
                    }

                    builder.reservations(reservationList);
                });
    }

    private String calculateRemainingTimeByStatus(Reservation res) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration;

        switch (res.getStatus()) {
            case reserved -> duration = Duration.between(now, res.getStartTime());
            case confirmed -> duration = Duration.between(now, res.getConfirmedAt().plusMinutes(2));
            case running -> {
                return fetchRemainingTimeFromSmartThings(res.getMachine().getDeviceId());
            }
            default -> duration = Duration.ZERO;
        }

        if (duration.isNegative()) return "00:00:00";

        long h = duration.toHours();
        long m = duration.toMinutes() % 60;
        long s = duration.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private String fetchRemainingTimeFromSmartThings(String deviceId) {
        try {
            SmartThingsToken token = smartThingsTokenService.getToken();
            JsonNode status = webClient.get()
                    .uri("https://api.smartthings.com/v1/devices/" + deviceId + "/status")
                    .headers(h -> h.setBearerAuth(token.getAccessToken()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode main = status.path("components").path("main");
            String time = main.path("washerOperatingState").path("completionTime").path("value").asText(null);
            if (time == null || time.equals("none")) {
                time = main.path("dryerOperatingState").path("completionTime").path("value").asText(null);
            }

            if (time == null || time.equals("none")) return "00:00:00";

            ZonedDateTime utc = ZonedDateTime.parse(time);
            LocalDateTime local = utc.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
            Duration d = Duration.between(LocalDateTime.now(), local);

            if (d.isNegative()) return "00:00:00";

            long h = d.toHours();
            long m = d.toMinutes() % 60;
            long s = d.getSeconds() % 60;
            return String.format("%02d:%02d:%02d", h, m, s);

        } catch (Exception e) {
            return "00:00:00";
        }
    }

    private String getDeviceType(String label) {
        return label.toLowerCase().contains("washer") ? "washer" : "dryer";
    }

    private String extractFloorFromLabel(String label) {
        for (String part : label.split("-")) {
            if (part.matches("\\d+F")) return part;
        }
        return "Unknown";
    }

    private JsonNode getDeviceStatus(SmartThingsToken token, String deviceId) {
        return webClient.get()
                .uri("https://api.smartthings.com/v1/devices/" + deviceId + "/status")
                .headers(headers -> headers.setBearerAuth(token.getAccessToken()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    private String getSafeValue(JsonNode main, String category, String field) {
        JsonNode node = main.path(category).path(field).path("value");
        return node.isMissingNode() ? "unknown" : node.asText();
    }

    private String getRemainingTime(JsonNode main, String type) {
        JsonNode node = type.equals("washer")
                ? main.path("washerOperatingState").path("completionTime").path("value")
                : main.path("dryerOperatingState").path("completionTime").path("value");

        if (node.isMissingNode() || node.asText().isEmpty() || node.asText().equals("none")) {
            return "00:00:00";
        }

        try {
            ZonedDateTime utcTime = ZonedDateTime.parse(node.asText());
            LocalDateTime localCompletionTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
            Duration duration = Duration.between(LocalDateTime.now(), localCompletionTime);

            if (duration.isNegative()) return "00:00:00";

            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (Exception e) {
            return "00:00:00";
        }
    }
}