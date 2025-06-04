package com.washer.Things.domain.smartThingsToken.presentation;

import com.washer.Things.domain.smartThingsToken.service.SmartThingsTokenService;
import com.washer.Things.global.exception.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.washer.Things.domain.smartThingsToken.presentation.dto.response.SmartThingsTokenResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class SmartThingsTokenController {
    private final SmartThingsTokenService smartThingsTokenService;
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<Void>> callback(@RequestParam("code") String code) {
        SmartThingsTokenResponse tokenResponse = smartThingsTokenService.exchangeCode(code);
        smartThingsTokenService.saveToken(tokenResponse);
        return ResponseEntity.ok(ApiResponse.success("SmartThings 인증 및 토큰 저장 성공"));
    }

}
