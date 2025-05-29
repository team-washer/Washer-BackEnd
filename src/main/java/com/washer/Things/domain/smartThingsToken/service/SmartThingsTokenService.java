package com.washer.Things.domain.smartThingsToken.service;

import com.washer.Things.domain.smartThingsToken.presentation.dto.response.SmartThingsTokenResponse;

public interface SmartThingsTokenService {
    void saveToken(SmartThingsTokenResponse tokenResponse);

    String getMyDevices();
    SmartThingsTokenResponse exchangeCode(String code);
}
