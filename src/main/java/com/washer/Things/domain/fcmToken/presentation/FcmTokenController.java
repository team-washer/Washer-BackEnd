package com.washer.Things.domain.fcmToken.presentation;

import com.washer.Things.domain.fcmToken.entity.FcmToken;
import com.washer.Things.domain.fcmToken.presentation.dto.request.FcmTokenRequest;
import com.washer.Things.domain.fcmToken.repository.FcmTokenRepository;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.global.util.UserUtil;
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

    private final UserUtil userUtil;
    private final FcmTokenRepository fcmTokenRepository;

    @PostMapping
    public ResponseEntity<Void> saveToken(@RequestBody FcmTokenRequest request) {
        User user = userUtil.getCurrentUser();
        Optional<FcmToken> existingTokenOpt = fcmTokenRepository.findByUser(user);

        if (existingTokenOpt.isPresent()) {
            FcmToken existingToken = existingTokenOpt.get();
            existingToken.setToken(request.getToken());
            existingToken.setPlatform(request.getPlatform());
            fcmTokenRepository.save(existingToken);
        } else {
            FcmToken newToken = new FcmToken();
            newToken.setUser(user);
            newToken.setToken(request.getToken());
            newToken.setPlatform(request.getPlatform());
            fcmTokenRepository.save(newToken);
        }
        return ResponseEntity.ok().build();
    }
}
