package com.washer.Things.domain.fcmToken.presentation.dto.request;

import com.washer.Things.domain.fcmToken.entity.enums.PlatformType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmTokenRequest {
    private String token;
    private PlatformType platform;
}
