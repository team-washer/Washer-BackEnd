package com.washer.Things.domain.auth.service;


import com.washer.Things.domain.auth.presentation.dto.response.ReissueTokenResponse;
import com.washer.Things.domain.auth.presentation.dto.response.SignInResponse;

public interface RefreshService {
    ReissueTokenResponse execute(String resolveRefreshToken);
}
