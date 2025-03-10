package com.washer.Things.domain.auth.service;


import com.washer.Things.domain.auth.presentation.dto.request.SignupRequest;
import com.washer.Things.domain.auth.presentation.dto.response.TokenResponse;

public interface SignupService {
    TokenResponse signup(SignupRequest request);
}
