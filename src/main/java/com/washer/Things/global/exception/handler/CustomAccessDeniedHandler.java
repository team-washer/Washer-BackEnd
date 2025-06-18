package com.washer.Things.global.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        HttpStatus forbidden = HttpStatus.FORBIDDEN;

        String responseString = objectMapper.writeValueAsString(Map.of(
                "success", false,
                "error", Map.of(
                        "code", "FORBIDDEN",
                        "message", "해당 엔드포인트에 대한 권한이 없습니다."
                ),
                "timestamp", Instant.now().toString()
        ));

        log.error("{}", responseString);

        response.setCharacterEncoding("UTF-8");
        response.setStatus(forbidden.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(responseString);
    }
}