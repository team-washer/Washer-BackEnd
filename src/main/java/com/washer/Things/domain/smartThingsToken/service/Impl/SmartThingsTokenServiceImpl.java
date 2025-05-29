package com.washer.Things.domain.smartThingsToken.service.Impl;

import com.washer.Things.domain.smartThingsToken.entity.SmartThingsToken;
import com.washer.Things.domain.smartThingsToken.presentation.dto.response.SmartThingsTokenResponse;
import com.washer.Things.domain.smartThingsToken.repository.SmartThingsTokenRepository;
import com.washer.Things.domain.smartThingsToken.service.SmartThingsTokenService;
import com.washer.Things.global.exception.HttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SmartThingsTokenServiceImpl implements SmartThingsTokenService {
    private final SmartThingsTokenRepository tokenRepository;
    private final WebClient webClient;

    @Value("${smartthings.client-id}")
    private String clientId;

    @Value("${smartthings.client-secret}")
    private String clientSecret;

    @Value("${smartthings.redirect-uri}")
    private String redirectUri;

    @Value("${smartthings.token-uri}")
    private String tokenUri;

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

    public SmartThingsTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);

        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(SmartThingsTokenResponse.class)
                .block();
    }
    public void refreshTokenIfNeeded() {
        SmartThingsToken token = tokenRepository.findTopByOrderByIdAsc().orElse(null);
        if (token == null) {
            throw new HttpException("NO_TOKEN", HttpStatus.NOT_FOUND, "SmartThings 토큰을 찾을 수 없습니다.");
        }

        Instant now = Instant.now();
        Instant expiresAt = token.getExpiresAt();

        if (expiresAt == null || now.isAfter(expiresAt.minusSeconds(300))) {
            refreshAccessToken(token.getRefreshToken());
        }
    }

    public void refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        SmartThingsTokenResponse tokenResponse = webClient.post()
                .uri(tokenUri)
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
            throw new HttpException("REFRESH_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "SmartThings 토큰 갱신에 실패했습니다.");
        }
    }

    public String getMyDevices() {
        refreshTokenIfNeeded();

        SmartThingsToken token = getToken();

        return webClient.get()
                .uri("https://api.smartthings.com/v1/devices")
                .headers(headers -> headers.setBearerAuth(token.getAccessToken()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public SmartThingsToken getToken() {
        return tokenRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new HttpException("NO_TOKEN", HttpStatus.NOT_FOUND, "SmartThings 토큰이 존재하지 않습니다."));
    }

}
