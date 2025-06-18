package com.washer.Things.domain.user.presentation.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserInfoResponse {
    private Long id;
    private String name;
    private String schoolNumber;
    private String gender;
    private String roomName;
    private String restrictedUntil;
    private String restrictionReason;
}