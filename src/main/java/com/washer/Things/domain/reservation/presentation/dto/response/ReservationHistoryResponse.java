package com.washer.Things.domain.reservation.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationHistoryResponse {
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime pausedSince;
    private LocalDateTime confirmedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String machineLabel;
}
