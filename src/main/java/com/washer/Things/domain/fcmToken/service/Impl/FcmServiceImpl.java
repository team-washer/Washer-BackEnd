package com.washer.Things.domain.fcmToken.service.Impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.washer.Things.domain.fcmToken.entity.FcmToken;
import com.washer.Things.domain.fcmToken.entity.enums.PlatformType;
import com.washer.Things.domain.fcmToken.repository.FcmTokenRepository;
import com.washer.Things.domain.fcmToken.service.FcmService;
import com.washer.Things.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final FcmTokenRepository fcmTokenRepository;

    @Override
    public void saveToken(User user, String token, PlatformType platform) {
        Optional<FcmToken> existingToken = fcmTokenRepository.findByUserAndToken(user, token);
        if (existingToken.isEmpty()) {
            FcmToken newToken = FcmToken.builder()
                    .user(user)
                    .token(token)
                    .platform(platform)
                    .build();
            fcmTokenRepository.save(newToken);
        }
    }

    public void sendToRoom(List<User> users, String title, String body) {
        List<User> distinctUsers = users.stream().distinct().toList();
        List<FcmToken> tokens = fcmTokenRepository.findAllByUserIn(distinctUsers);

        for (FcmToken token : tokens) {
            sendMessage(token.getToken(), title, body);
        }
    }

    private void sendMessage(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패", e);
        }
    }
}
