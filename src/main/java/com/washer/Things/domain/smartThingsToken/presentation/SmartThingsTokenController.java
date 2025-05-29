package com.washer.Things.domain.smartThingsToken.presentation;

import com.washer.Things.domain.smartThingsToken.service.SmartThingsTokenService;
import com.washer.Things.global.exception.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.washer.Things.domain.smartThingsToken.presentation.dto.response.SmartThingsTokenResponse;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class SmartThingsTokenController {
    private final SmartThingsTokenService smartThingsTokenService;
    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<String>> getMyDevices() {
        String devices = smartThingsTokenService.getMyDevices();
        return ResponseEntity.ok(ApiResponse.success(devices, "SmartThings 디바이스 조회 성공"));
    }

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<Void>> callback(@RequestParam("code") String code) {
            SmartThingsTokenResponse tokenResponse = smartThingsTokenService.exchangeCode(code);
        smartThingsTokenService.saveToken(tokenResponse);
        return ResponseEntity.ok(ApiResponse.success("SmartThings 인증 및 토큰 저장 성공"));
    }

    @PostMapping("/smartapp")
    public ResponseEntity<Object> handleSmartAppWebhook(@RequestBody Map<String, Object> payload) {
        log.info("SmartApp Webhook payload received: {}", payload);

        String lifecycle = (String) payload.get("lifecycle");
        if (lifecycle == null) {
            log.warn("Lifecycle is missing in payload");
            return ResponseEntity.badRequest().body(Map.of("error", "lifecycle missing"));
        }

        switch (lifecycle) {
            case "PING":
                return ResponseEntity.ok(Map.of("pingData", Map.of("status", "OK")));

            case "INSTALL":
                // 설치 시 초기화 작업 등 필요하면 수행
                return ResponseEntity.ok(Map.of("installData", Map.of("state", "COMPLETE")));

            case "UPDATE":
                // 업데이트 시 처리
                return ResponseEntity.ok(Map.of("updateData", Map.of("state", "COMPLETE")));

            case "EVENT":
                // 이벤트 처리 로직 필요하면 작성
                // 예: event 데이터 로그 출력 또는 내부 처리 호출
                log.info("EVENT received: {}", payload.get("event"));
                return ResponseEntity.ok().build();

            default:
                log.warn("Unknown lifecycle: {}", lifecycle);
                return ResponseEntity.badRequest().body(Map.of("error", "Unknown lifecycle"));
        }
    }
}
