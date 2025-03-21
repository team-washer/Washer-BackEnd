package com.washer.Things.global.security.jwt;

import com.washer.Things.domain.auth.presentation.dto.response.TokenResponse;
import com.washer.Things.domain.user.entity.Role;
import com.washer.Things.global.entity.TokenType;
import com.washer.Things.global.exception.HttpException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final UserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.access}")
    private Long access;
    @Value("${jwt.refresh}")
    private Long refresh;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public TokenResponse generateTokenSet(Long id, Role role){
        Long now = System.currentTimeMillis();
        Long accessTokenExpires = now + access;
        Long refreshTokenExpires = now + refresh;

        String accessToken = generateToken(id, role, accessTokenExpires, TokenType.ACCESS);
        String refreshToken = generateToken(id, role, refreshTokenExpires, TokenType.REFRESH);

        return new TokenResponse(
                accessToken,
                convertToIso8601Kst(accessTokenExpires),
                refreshToken,
                convertToIso8601Kst(refreshTokenExpires),
                role
        );
    }

    private String generateToken(Long userId, Role role, Long expiresAt, TokenType tokenType) {
        return Jwts.builder()
                .signWith(getSigningKey())
                .subject(String.valueOf(userId))
                .claims(Map.of("role", role))
                .issuedAt(new Date())
                .expiration(new Date(expiresAt))
                .compact();
    }


    private String convertToIso8601Kst(Long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getTokenSubject(token));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private String getTokenSubject(String subject) {
        return getClaims(subject).getSubject();
    }

    public Claims getClaims(String token) {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);

        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new HttpException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new HttpException(HttpStatus.FORBIDDEN, "형식이 일치하지 않는 토큰입니다.");
        } catch (MalformedJwtException e) {
            throw new HttpException(HttpStatus.FORBIDDEN, "올바르지 않은 구성의 토큰입니다.");
        } catch (SignatureException e) {
            throw new HttpException(HttpStatus.FORBIDDEN, "서명을 확인할 수 없는 토큰입니다.");
        } catch (RuntimeException e) {
            throw new HttpException(HttpStatus.FORBIDDEN, "알 수 없는 토큰입니다.");
        }
    }

    public Boolean validateToken(String token){
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.after(new Date());
        } catch (HttpException e) {
            return false;
        }
    }
}