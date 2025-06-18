package com.washer.Things.domain.reservation.presentation.dto.response;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReservationResponse {
    private Long reservationId;
    private String machineLabel;
    private String status;
    private LocalDateTime startTime;
    private String remainingTime;
}
