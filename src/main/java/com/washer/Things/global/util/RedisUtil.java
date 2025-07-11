package com.washer.Things.global.util;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    public void setRefreshToken(String userId, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, ttlMillis, TimeUnit.MILLISECONDS);
    }

    public Optional<String> getRefreshToken(String userId) {
        Object token = redisTemplate.opsForValue().get("refresh:" + userId);
        return Optional.ofNullable(token != null ? token.toString() : null);
    }

    public void deleteRefreshToken(String userId) {
        redisTemplate.delete("refresh:" + userId);
    }
}
