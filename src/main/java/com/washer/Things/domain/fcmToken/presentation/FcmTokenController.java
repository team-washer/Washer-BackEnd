package com.washer.Things.domain.fcmToken.presentation;

import com.washer.Things.domain.fcmToken.entity.FcmToken;
import com.washer.Things.domain.fcmToken.presentation.dto.request.FcmNotificationRequest;
import com.washer.Things.domain.fcmToken.presentation.dto.request.FcmTokenRequest;
import com.washer.Things.domain.fcmToken.repository.FcmTokenRepository;
import com.washer.Things.domain.fcmToken.service.FcmService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.service.UserService;
import com.washer.Things.global.exception.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm-token")
public class FcmTokenController {

    private final UserService userService;
    private final FcmService fcmService;
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveToken(@RequestBody FcmTokenRequest request) {
        User user = userService.getCurrentUser();
        fcmService.saveToken(user, request.getToken(), request.getPlatform());
        return ResponseEntity.ok(ApiResponse.success("fcm 토큰 저장 성공"));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Void>> sendTestNotification(@RequestBody FcmNotificationRequest request) {
        fcmService.sendNotificationToUser(request.getUserId(), request.getTitle(), request.getBody());
        return ResponseEntity.ok(ApiResponse.success("알림 전송 성공"));
    }
}
