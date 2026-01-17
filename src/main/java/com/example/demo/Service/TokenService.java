package com.example.demo.Service;


import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Resource
    private StringRedisTemplate redis;

    private static final long TOKEN_EXPIRE = 2L; // hours

    public void saveLoginSession(Long userId, String uuid) {

        // userId -> uuid
        redis.opsForValue().set(
                "user:uuid:" + userId,
                uuid,
                TOKEN_EXPIRE,
                TimeUnit.HOURS
        );

        // uuid -> userId
        redis.opsForValue().set(
                "uuid:user:" + uuid,
                String.valueOf(userId),
                TOKEN_EXPIRE,
                TimeUnit.HOURS
        );
    }

    public Long getUserIdByUUID(String uuid) {
        String userId = redis.opsForValue().get("uuid:user:" + uuid);
        return userId == null ? null : Long.valueOf(userId);
    }

    public String getUUIDByUserId(Long userId) {
        return redis.opsForValue().get("user:uuid:" + userId);
    }

    public boolean isUUIDValid(Long userId, String uuid) {
        // Redis 中的 token 必须和传入一致
        String cachedToken = redis.opsForValue().get("user:uuid:" + userId);
        return uuid.equals(cachedToken);
    }

    public void logout(Long userId, String uuid) {
        redis.delete("user:uuid:" + userId);
        redis.delete("uuid:user:" + uuid);
    }

    public Map<Object, Object> getRefreshToken(String refreshToken){
        return redis.opsForHash().entries("refreshToken:"+refreshToken);
    }
}