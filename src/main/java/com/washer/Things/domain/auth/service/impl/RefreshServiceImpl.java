package com.washer.Things.domain.auth.service.impl;

import com.washer.Things.domain.auth.presentation.dto.response.TokenResponse;
import com.washer.Things.domain.auth.service.RefreshService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.domain.user.repository.UserRepository;
import com.washer.Things.global.exception.HttpException;
import com.washer.Things.global.security.jwt.TokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshServiceImpl implements RefreshService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    @Transactional
    public TokenResponse refresh(String refresh){
        refresh = refresh.substring(7);

        Boolean validateRefresh = tokenProvider.validateToken(refresh);
        if (!validateRefresh) {
            throw new HttpException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = Long.parseLong(tokenProvider.getClaims(refresh).getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "해당하는 유저를 찾을 수 없습니다."));

        return tokenProvider.generateTokenSet(user.getId(), user.getRoles().get(0));
    }
}
