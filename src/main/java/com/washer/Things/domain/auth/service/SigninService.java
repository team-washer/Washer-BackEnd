package com.washer.Things.domain.auth.service;

import com.washer.Things.domain.auth.presentation.dto.request.SigninRequest;
import com.washer.Things.domain.auth.presentation.dto.response.TokenResponse;

public interface SigninService {
    TokenResponse signin(SigninRequest request);
}
