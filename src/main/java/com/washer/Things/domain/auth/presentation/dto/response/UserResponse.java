package com.washer.Things.domain.auth.presentation.dto.response;

import lombok.Builder;

@Builder
public record UserResponse(
        String id,
        String name,
        String roomNumber,
        String gender,
        String restrictedUntil,
        String restrictionReason
) {}