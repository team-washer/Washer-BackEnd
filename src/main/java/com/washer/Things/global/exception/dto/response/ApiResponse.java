package com.washer.Things.global.exception.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ApiError error;

    private String message;

    private Instant timestamp;

    // 성공 응답 생성
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    // 에러 응답 생성
    public static ApiResponse<Void> error(HttpStatus statusCode, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(new ApiError(statusCode, message, null))
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiResponse<Void> error(HttpStatus statusCode, String message, Object details) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(new ApiError(statusCode, message, details))
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ApiError {
        private final HttpStatus statusCode;
        private final String message;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private final Object details;
    }
}
