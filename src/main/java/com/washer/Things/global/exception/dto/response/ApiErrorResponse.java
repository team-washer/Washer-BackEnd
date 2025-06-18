package com.washer.Things.global.exception.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;
@Getter
public class ApiErrorResponse {
    private final boolean success = false;
    private final ErrorDetail error;
    private final String timestamp = java.time.Instant.now().toString();

    public ApiErrorResponse(HttpStatus statusCode, String message) {
        this.error = new ErrorDetail(statusCode, message);
    }
    @Getter
    @Builder
    @AllArgsConstructor
    public static class ErrorDetail {
        private final HttpStatus statusCode;
        private final String message;
    }
}