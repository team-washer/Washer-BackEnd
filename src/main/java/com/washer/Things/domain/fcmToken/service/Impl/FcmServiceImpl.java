package com.washer.Things.domain.fcmToken.service.Impl;

import com.google.firebase.messaging.*;
import com.washer.Things.domain.fcmToken.entity.FcmToken;
import com.washer.Things.domain.fcmToken.entity.enums.PlatformType;
import com.washer.Things.domain.fcmToken.repository.FcmTokenRepository;
import com.washer.Things.domain.fcmToken.service.FcmService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.repository.UserRepository;
import com.washer.Things.global.exception.HttpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;
    @Override
    public void saveToken(User user, String token, PlatformType platform) {
        Optional<FcmToken> existingToken = fcmTokenRepository.findByUserAndPlatform(user, platform);

        if (existingToken.isPresent()) {
            FcmToken fcmToken = existingToken.get();
            fcmToken.updateToken(token);
        } else {
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
        List<FcmToken> tokens = fcmTokenRepository.findLatestTokenPerUser(distinctUsers);

        for (FcmToken token : tokens) {
            sendMessage(token.getToken(), title, body);
        }
    }

    private void sendMessage(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .putData("title", title)
                    .putData("body", body)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패", e);
        }
    }

    public void sendNotificationToUser(Long userId, String title, String body) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다"));

        FcmToken token = fcmTokenRepository.findTopByUserOrderByIdDesc(user)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "FCM 토큰이 존재하지 않습니다"));

        sendMessage(token.getToken(), title, body);
    }

}
