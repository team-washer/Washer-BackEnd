package com.washer.Things.domain.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private String schoolNumber;
    private String roomNumber;
    private String gender;
    private String restrictedUntil;
    private String restrictionReason;
    private Long reservationId;
    private String machineLabel;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime completedAt;
    private String remainingTime;
}