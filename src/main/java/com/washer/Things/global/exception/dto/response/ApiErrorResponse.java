package com.washer.Things.global.exception.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
@Getter
public class ApiErrorResponse {
    private final boolean success = false;
    private final ErrorDetail error;
    private final String timestamp = java.time.Instant.now().toString();

    public ApiErrorResponse(String code, String message, Map<String, Object> details) {
        this.error = new ErrorDetail(code, message, details);
    }
    @Getter
    @Builder
    @AllArgsConstructor
    public static class ErrorDetail {
        private final String code;
        private final String message;
        private final Map<String, Object> details;
    }
}