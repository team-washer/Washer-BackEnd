package com.washer.Things.domain.machine.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class DeviceInfoResponse {
    private String label;
    private String floor;
    private String powerState;
    private String machineState;
    private String jobState;
    private String remainingTime;
    private Boolean isOutOfOrder;
    private LocalDateTime nextAvailableAt;
    private List<Map<String, Object>> reservations;
}
