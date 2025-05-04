package com.washer.Things.domain.auth.service.impl;

import com.washer.Things.domain.auth.presentation.dto.response.ReissueTokenResponse;
import com.washer.Things.domain.auth.service.RefreshService;
import com.washer.Things.global.entity.JwtType;
import com.washer.Things.global.exception.enums.ExceptionEnum;
import com.washer.Things.global.security.jwt.JwtProvider;
import com.washer.Things.global.security.jwt.dto.JwtDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshServiceImpl implements RefreshService {
    private final JwtProvider jwtProvider;

    public ReissueTokenResponse execute(String resolveRefreshToken) {
        Long currentUserId = Long.parseLong(jwtProvider.getIdByRefreshToken(resolveRefreshToken));

        if (!jwtProvider.validateToken(resolveRefreshToken, JwtType.REFRESH_TOKEN)) {
            throw ExceptionEnum.AUTH_EXPIRED_TOKEN.toHttpException();
        }

        JwtDetails newAccessToken = jwtProvider.generateToken(currentUserId, JwtType.ACCESS_TOKEN);
        JwtDetails newRefreshToken = jwtProvider.generateToken(currentUserId, JwtType.REFRESH_TOKEN);

        return new ReissueTokenResponse(
                newAccessToken.getToken(),
                newAccessToken.getExpiredAt(),
                newRefreshToken.getToken(),
                newRefreshToken.getExpiredAt()
        );
    }
}
