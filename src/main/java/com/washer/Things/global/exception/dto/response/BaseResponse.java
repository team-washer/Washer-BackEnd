package com.washer.Things.global.exception.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BaseResponse<T> {
    private boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ApiError error;

    private String message;

    private Instant timestamp;

    // 성공 응답 생성
    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> BaseResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static BaseResponse<Void> success(String message) {
        return BaseResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    // 에러 응답 생성
    public static BaseResponse<Void> error(HttpStatus statusCode, String message) {
        return BaseResponse.<Void>builder()
                .success(false)
                .error(new ApiError(statusCode, message, null))
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static BaseResponse<Void> error(HttpStatus statusCode, String message, Object details) {
        return BaseResponse.<Void>builder()
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
