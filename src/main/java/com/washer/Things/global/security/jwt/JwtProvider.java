package com.washer.Things.global.security.jwt;

import com.washer.Things.global.auth.AuthDetailsService;
import com.washer.Things.global.entity.JwtType;
import com.washer.Things.global.exception.HttpException;
import com.washer.Things.global.exception.enums.ExceptionEnum;
import com.washer.Things.global.security.jwt.dto.JwtDetails;
import com.washer.Things.global.util.DateUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final AuthDetailsService authDetailsService;
    private final JwtProperties jwtProperties;

    public JwtProvider(AuthDetailsService authDetailsService, JwtProperties jwtProperties) {
        this.authDetailsService = authDetailsService;
        this.jwtProperties = jwtProperties;
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String resolvedToken = resolveToken(token);
        Claims payload = getPayload(resolvedToken, JwtType.ACCESS_TOKEN);

        UserDetails userDetails = authDetailsService.loadUserByUsername(payload.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public boolean validateToken(String token, JwtType jwtType) {
        try {
            getPayload(token, jwtType);
            return true;
        } catch (HttpException e) {
            return false;
        }
    }

    public String resolveToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        return token.substring(7);
    }

    public String getIdByRefreshToken(String refreshToken) {
        return getPayload(refreshToken, JwtType.REFRESH_TOKEN).getSubject();
    }

    public Claims getPayload(String token, JwtType jwtType) {
        if (token == null) {
            throw ExceptionEnum.AUTH_EMPTY_TOKEN.toHttpException();
        }

        String tokenKey = jwtType == JwtType.ACCESS_TOKEN
                ? jwtProperties.getAccessTokenKey()
                : jwtProperties.getRefreshTokenKey();

        byte[] keyBytes = Base64.getEncoder().encode(tokenKey.getBytes());
        var signingKey = Keys.hmacShaKeyFor(keyBytes);

        try {
            return Jwts
                    .parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw ExceptionEnum.AUTH_EXPIRED_TOKEN.toHttpException();
        } catch (UnsupportedJwtException e) {
            throw ExceptionEnum.AUTH_UNSUPPORTED_TOKEN.toHttpException();
        } catch (MalformedJwtException e) {
            throw ExceptionEnum.AUTH_MALFORMED_TOKEN.toHttpException();
        } catch (RuntimeException e) {
            throw ExceptionEnum.AUTH_OTHER_TOKEN.toHttpException();
        }
    }

    public JwtDetails generateToken(Long id, JwtType jwtType) {
        long tokenExpires = jwtType == JwtType.ACCESS_TOKEN
                ? jwtProperties.getAccessTokenExpires()
                : jwtProperties.getRefreshTokenExpires();

        LocalDateTime expiredAt = LocalDateTime.now().plus(Duration.ofMillis(tokenExpires));

        String tokenKey = jwtType == JwtType.ACCESS_TOKEN
                ? jwtProperties.getAccessTokenKey()
                : jwtProperties.getRefreshTokenKey();

        byte[] keyBytes = Base64.getEncoder().encode(tokenKey.getBytes());
        var signingKey = Keys.hmacShaKeyFor(keyBytes);

        String token = Jwts
                .builder()
                .subject(String.valueOf(id))
                .signWith(signingKey)
                .issuedAt(new Date())
                .expiration(DateUtil.toDate(expiredAt))
                .compact();

        return new JwtDetails(token, expiredAt);
    }
}
