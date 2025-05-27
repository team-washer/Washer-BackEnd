package com.washer.Things.global.util;

import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseUtil {
    public static <T> ResponseEntity<Map<String, Object>> ok(T data, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("data", data);
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(body);
    }

    public static <T> ResponseEntity<Map<String, Object>> ok(T data) {
        return ok(data, "요청이 성공적으로 처리되었습니다.");
    }
}
