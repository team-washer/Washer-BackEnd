package com.washer.Things.domain.auth.service;


import com.washer.Things.domain.auth.presentation.dto.response.TokenResponse;

public interface RefreshService {
    TokenResponse refresh(String refresh);
}
