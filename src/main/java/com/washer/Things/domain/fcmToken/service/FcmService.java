package com.washer.Things.domain.fcmToken.service;

import com.washer.Things.domain.fcmToken.entity.enums.PlatformType;
import com.washer.Things.domain.user.entity.User;

import java.util.List;

public interface FcmService {
    void saveToken(User user, String token, PlatformType platform);
    void sendToRoom(List<User> users, String title, String body);
}
