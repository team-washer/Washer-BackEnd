package com.washer.Things.domain.smartThingsToken.presentation;

import com.washer.Things.domain.smartThingsToken.service.SmartThingsTokenService;
import com.washer.Things.global.exception.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.washer.Things.domain.smartThingsToken.presentation.dto.response.SmartThingsTokenResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class SmartThingsTokenController {
    private final SmartThingsTokenService smartThingsTokenService;
    private final WebClient webClient;
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

    @GetMapping("/smartapp")
    public ResponseEntity<Object> handleSmartAppWebhook(@RequestBody Map<String, Object> payload) {
        log.info("SmartApp Webhook payload received: {}", payload);

        String lifecycle = (String) payload.get("lifecycle");
        if (lifecycle == null) {
            log.warn("Lifecycle is missing in payload");
            return ResponseEntity.badRequest().body(Map.of("error", "lifecycle missing"));
        }

        switch (lifecycle) {
            case "PING":
                Map<String, Object> pingData = (Map<String, Object>) payload.get("pingData");
                if (pingData == null || !pingData.containsKey("challenge")) {
                    log.warn("PING payload missing challenge");
                    return ResponseEntity.badRequest().body(Map.of("error", "challenge missing"));
                }
                String challenge = (String) pingData.get("challenge");
                return ResponseEntity.ok(Map.of("pingData", Map.of("challenge", challenge)));

            case "CONFIRMATION":
                Map<String, Object> confirmationData = (Map<String, Object>) payload.get("confirmationData");
                if (confirmationData == null || !confirmationData.containsKey("confirmationUrl")) {
                    log.warn("CONFIRMATION payload missing confirmationUrl");
                    return ResponseEntity.badRequest().body(Map.of("error", "confirmationUrl missing"));
                }
                String confirmationUrl = (String) confirmationData.get("confirmationUrl");

                log.info("Sending GET to confirmationUrl: {}", confirmationUrl);

                try {
                    String response = webClient.get()
                            .uri(confirmationUrl)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();  // 동기 호출

                    log.info("Confirmation success: {}", response);
                } catch (Exception e) {
                    log.error("Confirmation failed", e);
                    return ResponseEntity.status(500).body(Map.of("error", "Confirmation failed"));
                }

                return ResponseEntity.ok(Map.of());

            default:
                log.warn("Unknown lifecycle: {}", lifecycle);
                return ResponseEntity.badRequest().body(Map.of("error", "Unknown lifecycle"));
        }
    }

}
