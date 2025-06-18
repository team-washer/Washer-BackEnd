package com.washer.Things.domain.smartThingsToken.service;

import com.washer.Things.domain.smartThingsToken.entity.SmartThingsToken;
import com.washer.Things.domain.smartThingsToken.presentation.dto.response.SmartThingsTokenResponse;


public interface SmartThingsTokenService {
    void saveToken(SmartThingsTokenResponse tokenResponse);
    SmartThingsTokenResponse exchangeCode(String code);
    SmartThingsToken getToken();
    void refreshTokenIfNeeded();
}
