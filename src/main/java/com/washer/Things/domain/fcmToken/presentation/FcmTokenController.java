package com.washer.Things.domain.fcmToken.presentation;

import com.washer.Things.domain.fcmToken.presentation.dto.request.FcmNotificationRequest;
import com.washer.Things.domain.fcmToken.presentation.dto.request.FcmTokenRequest;
import com.washer.Things.domain.fcmToken.service.FcmService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.service.UserService;
import com.washer.Things.global.exception.dto.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm-token")
public class FcmTokenController {

    private final UserService userService;
    private final FcmService fcmService;
    @Operation(summary = "FCM 토큰 저장")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 전송 성공"),
            @ApiResponse(responseCode = "500", description = "알림 전송 실패 또는 예기치 못한 오류")
    })
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> saveToken(@RequestBody FcmTokenRequest request) {
        User user = userService.getCurrentUser();
        fcmService.saveToken(user, request.getToken(), request.getPlatform());
        return ResponseEntity.ok(BaseResponse.success("fcm 토큰 저장 성공"));
    }

    @Operation(summary = "FCM 알림 전송 테스트 (관리자)")
    @PostMapping("/test")
    public ResponseEntity<BaseResponse<Void>> sendTestNotification(@RequestBody FcmNotificationRequest request) {
        fcmService.sendNotificationToUser(request.getUserId(), request.getTitle(), request.getBody());
        return ResponseEntity.ok(BaseResponse.success("알림 전송 성공"));
    }
}
