package com.washer.Things.domain.fcmToken.service;

import com.washer.Things.domain.fcmToken.repository.FcmTokenRepository;
import com.washer.Things.domain.user.entity.User;

import java.util.List;

public interface FcmService {
    void sendMessage(String token, String title, String body);
    void sendToRoom(List<User> users, String title, String body, FcmTokenRepository tokenRepository);
}
