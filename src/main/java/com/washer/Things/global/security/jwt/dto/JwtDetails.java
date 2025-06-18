package com.washer.Things.global.security.jwt.dto;

import java.time.LocalDateTime;

public class JwtDetails {

    private final String token;
    private final LocalDateTime expiredAt;

    public JwtDetails(String token, LocalDateTime expiredAt) {
        this.token = token;
        this.expiredAt = expiredAt;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }
}