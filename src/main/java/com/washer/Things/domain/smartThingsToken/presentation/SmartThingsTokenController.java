package com.washer.Things.domain.smartThingsToken.presentation;

import com.washer.Things.domain.smartThingsToken.service.SmartThingsTokenService;
import com.washer.Things.global.exception.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.washer.Things.domain.smartThingsToken.presentation.dto.response.SmartThingsTokenResponse;
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
    public ResponseEntity<String> handleSmartAppWebhook(@RequestBody String payload) {
        log.info("SmartApp Webhook payload received: {}", payload);

        if (payload.contains("\"lifecycle\":\"PING\"")) {
            return ResponseEntity.ok("{\"pingData\": {\"status\": \"OK\"}}");
        }

        return ResponseEntity.ok("OK");
    }
}
