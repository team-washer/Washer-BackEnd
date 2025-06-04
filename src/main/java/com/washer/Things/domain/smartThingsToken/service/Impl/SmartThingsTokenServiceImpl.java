package com.washer.Things.domain.smartThingsToken.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.washer.Things.domain.smartThingsToken.entity.SmartThingsToken;
import com.washer.Things.domain.smartThingsToken.presentation.dto.response.SmartThingsTokenResponse;
import com.washer.Things.domain.smartThingsToken.repository.SmartThingsTokenRepository;
import com.washer.Things.domain.smartThingsToken.service.SmartThingsTokenService;
import com.washer.Things.global.exception.HttpException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmartThingsTokenServiceImpl implements SmartThingsTokenService {
    private final SmartThingsTokenRepository tokenRepository;
    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.smartthings.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.smartthings.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.smartthings.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.smartthings.token-uri}")
    private String tokenUri;

    @Transactional
    public void saveToken(SmartThingsTokenResponse tokenResponse) {
        tokenRepository.deleteAll();

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(tokenResponse.getExpiresIn());

        SmartThingsToken token = new SmartThingsToken();

        token.setAccessToken(tokenResponse.getAccessToken());
        token.setRefreshToken(tokenResponse.getRefreshToken());
        token.setIssuedAt(now);
        token.setExpiresAt(expiresAt);

        tokenRepository.save(token);
    }

    @Transactional
    public SmartThingsTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        return webClient.post()
                .uri(tokenUri)
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(SmartThingsTokenResponse.class)
                .block();
    }

    @Transactional
    public void refreshTokenIfNeeded() {
        SmartThingsToken token = tokenRepository.findTopByOrderByIdAsc().orElse(null);
        if (token == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "SmartThings 토큰을 찾을 수 없습니다.");
        }

        Instant now = Instant.now();
        Instant expiresAt = token.getExpiresAt();

        if (expiresAt == null || now.isAfter(expiresAt.minusSeconds(300))) {
            refreshAccessToken(token.getRefreshToken());
        }
    }

    @Transactional
    public void refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);

        SmartThingsTokenResponse tokenResponse = webClient.post()
                .uri(tokenUri)
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(SmartThingsTokenResponse.class)
                .block();

        if (tokenResponse != null) {
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(tokenResponse.getExpiresIn());

            SmartThingsToken token = tokenRepository.findTopByOrderByIdAsc().orElse(new SmartThingsToken());
            token.setAccessToken(tokenResponse.getAccessToken());
            token.setRefreshToken(tokenResponse.getRefreshToken());
            token.setIssuedAt(now);
            token.setExpiresAt(expiresAt);

            tokenRepository.save(token);
        } else {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "SmartThings 토큰 갱신에 실패했습니다.");
        }
    }


    @Transactional
    public Map<String, List<Map<String, Object>>> getMyDevices() {
        refreshTokenIfNeeded();
        SmartThingsToken token = getToken();

        String rawResponse = webClient.get()
                .uri("https://api.smartthings.com/v1/devices")
                .headers(headers -> headers.setBearerAuth(token.getAccessToken()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode items = mapper.readTree(rawResponse).path("items");

            Map<String, List<Map<String, Object>>> result = new HashMap<>();

            for (JsonNode device : items) {
                String label = device.path("label").asText();
                String deviceId = device.path("deviceId").asText();
                String type = getDeviceType(label);

                JsonNode status = getDeviceStatus(token, deviceId);
                JsonNode main = status.path("components").path("main");

                Map<String, Object> deviceInfo = new HashMap<>();
                deviceInfo.put("label", label);
                deviceInfo.put("floor", extractFloorFromLabel(label));
                deviceInfo.put("powerState", getSafeValue(main, "switch", "switch"));

                if (type.equals("washer")) {
                    String machineState = getSafeValue(main, "washerOperatingState", "machineState");
                    String washerJobState = getSafeValue(main, "washerOperatingState", "washerJobState");
                    deviceInfo.put("machineState", machineState);
                    deviceInfo.put("washerJobState", washerJobState);
                } else {
                    String machineState = getSafeValue(main, "dryerOperatingState", "machineState");
                    String dryerJobState = getSafeValue(main, "dryerOperatingState", "dryerJobState");
                    deviceInfo.put("machineState", machineState);
                    deviceInfo.put("dryerJobState", dryerJobState);
                }

                deviceInfo.put("remainingTime", getRemainingTime(main, type));

                result.computeIfAbsent(type, k -> new ArrayList<>()).add(deviceInfo);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("디바이스 정보 처리 실패", e);
        }
    }
    private String getDeviceType(String label) {
        return label.toLowerCase().contains("washer") ? "washer" : "dryer";
    }

    private JsonNode getDeviceStatus(SmartThingsToken token, String deviceId) {
        return webClient.get()
                .uri("https://api.smartthings.com/v1/devices/" + deviceId + "/status")
                .headers(headers -> headers.setBearerAuth(token.getAccessToken()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    private String getSafeValue(JsonNode main, String category, String field) {
        JsonNode node = main.path(category).path(field).path("value");
        return node.isMissingNode() ? "unknown" : node.asText();
    }

    private String getRemainingTime(JsonNode main, String type) {
        JsonNode node = type.equals("washer")
                ? main.path("samsungce.washerWashingTime").path("completionTime").path("value")
                : main.path("samsungce.dryerDryingTime").path("completionTime").path("value");

        return (node.isMissingNode() || node.asText().isEmpty()) ? "none" : node.asText();
    }

    private String extractFloorFromLabel(String label) {
        for (String part : label.split("-")) {
            if (part.matches("\\d+F")) return part;
        }
        return "Unknown";
    }

    @Transactional
    public SmartThingsToken getToken() {
        return tokenRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "SmartThings 토큰이 존재하지 않습니다."));
    }

}
