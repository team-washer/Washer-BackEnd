package com.washer.Things.domain.machine.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.machine.presentation.dto.response.DeviceInfoResponse;
import com.washer.Things.domain.machine.repository.MachineRepository;
import com.washer.Things.domain.machine.service.MachineInfoService;
import com.washer.Things.domain.smartThingsToken.entity.SmartThingsToken;
import com.washer.Things.domain.smartThingsToken.service.Impl.SmartThingsTokenServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MachineInfoServiceImpl implements MachineInfoService {
    private final SmartThingsTokenServiceImpl smartThingsTokenService;
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
                    builder.isOutOfOrder(machine.isOutOfOrder());
                    builder.nextAvailableAt(machine.getNextAvailableAt());
                    builder.reservations(machine.getReservations().stream()
                            .map(res -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("id", res.getId());
                                map.put("status", res.getStatus());
                                map.put("startTime", res.getStartTime());
                                return map;
                            }).collect(Collectors.toList()));
                });
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
                ? main.path("samsungce.washerWashingTime").path("completionTime").path("value")
                : main.path("samsungce.dryerDryingTime").path("completionTime").path("value");

        return (node.isMissingNode() || node.asText().isEmpty()) ? "none" : node.asText();
    }
}
