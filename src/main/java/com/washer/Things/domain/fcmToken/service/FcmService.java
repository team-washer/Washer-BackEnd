package com.washer.Things.domain.fcmToken.service;

import com.washer.Things.domain.fcmToken.entity.FcmToken;
import com.washer.Things.domain.fcmToken.entity.enums.PlatformType;
import com.washer.Things.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface FcmService {
    void saveToken(User user, String token, PlatformType platform);
    void sendToRoom(List<User> users, String title, String body);

    void sendNotificationToUser(Long userId, String title, String body);
}
