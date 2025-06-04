package com.washer.Things.domain.smartThingsToken.service;

import com.washer.Things.domain.smartThingsToken.presentation.dto.response.SmartThingsTokenResponse;

import java.util.List;
import java.util.Map;

public interface SmartThingsTokenService {
    void saveToken(SmartThingsTokenResponse tokenResponse);
    SmartThingsTokenResponse exchangeCode(String code);
}
