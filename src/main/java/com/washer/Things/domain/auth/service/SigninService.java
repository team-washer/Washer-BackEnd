package com.washer.Things.domain.auth.service;

import com.washer.Things.domain.auth.presentation.dto.request.SigninRequest;
import com.washer.Things.domain.auth.presentation.dto.response.SignInResponse;

public interface SigninService {
    SignInResponse execute(SigninRequest request);
}
