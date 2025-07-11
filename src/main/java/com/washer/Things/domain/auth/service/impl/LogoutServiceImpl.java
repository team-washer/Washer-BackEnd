package com.washer.Things.domain.auth.service.impl;

import com.washer.Things.domain.auth.service.LogoutService;
import com.washer.Things.global.entity.JwtType;
import com.washer.Things.global.exception.HttpException;
import com.washer.Things.global.security.jwt.JwtProvider;
import com.washer.Things.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {
    private final RedisUtil redisUtil;
    private final JwtProvider jwtProvider;
    public void logout(String resolveRefreshToken) {
        String userId = jwtProvider.getPayload(resolveRefreshToken, JwtType.REFRESH_TOKEN).getSubject();

        Optional<String> savedRefreshTokenOpt = redisUtil.getRefreshToken(userId);

        if (savedRefreshTokenOpt.isEmpty()) {
            throw new HttpException(HttpStatus.NOT_FOUND, "리프레시 토큰을 찾을 수 없습니다.");
        }
        String savedRefreshToken = savedRefreshTokenOpt.get();
        if (!resolveRefreshToken.equals(savedRefreshToken)) {
            throw new HttpException(HttpStatus.UNAUTHORIZED, "잘못된 리프레시 토큰입니다.");
        }
        redisUtil.deleteRefreshToken(userId);
    }
}
