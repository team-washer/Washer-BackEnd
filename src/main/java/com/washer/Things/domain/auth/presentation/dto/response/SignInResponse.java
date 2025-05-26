package com.washer.Things.domain.auth.presentation.dto.response;
import com.washer.Things.domain.user.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SignInResponse {
    private final String accessToken;
    private final LocalDateTime accessTokenExpiredAt;
    private final String refreshToken;
    private final LocalDateTime refreshTokenExpiredAt;
    private final Role role;
}