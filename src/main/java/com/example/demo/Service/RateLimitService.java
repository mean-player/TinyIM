package com.example.demo.Service;

import com.example.demo.Component.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String KEY_AVATAR = "limit:avatar:";
    private static final String KEY_NICKNAME = "limit:nickname:";
    private static final String KEY_SIGNATURE = "limit:signature:";
    private static final String KEY_APPLICATION = "limit:application:";
    private static final String KEY_CREATE_GROUP = "limit:creategroup:";
    private static final String KEY_SEND_MESSAGE = "limit:sendmessage:";
    private static final String KEY_UPLOAD = "limit:upload:";
    private static final String KEY_PASSWORD_RESET = "limit:passwordreset:";

    private final StringRedisTemplate redis;
    private final RateLimitProperties properties;

    public boolean canChangeAvatar(String userId) {
        return checkDailyLimit(KEY_AVATAR, userId, properties.getAvatarDailyLimit());
    }

    public boolean canChangeNickname(String userId) {
        String key = KEY_NICKNAME + userId;
        Boolean exists = redis.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return false;
        }
        redis.opsForValue().set(key, "1", properties.getNicknameCooldownMinutes(), TimeUnit.MINUTES);
        return true;
    }

    public boolean canChangeSignature(String userId) {
        String key = KEY_SIGNATURE + userId;
        Boolean exists = redis.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return false;
        }
        redis.opsForValue().set(key, "1", properties.getSignatureCooldownMinutes(), TimeUnit.MINUTES);
        return true;
    }

    public boolean canSendApplication(String userId) {
        return checkDailyLimit(KEY_APPLICATION, userId, properties.getApplicationDailyLimit());
    }

    public boolean canCreateGroup(String userId) {
        return checkDailyLimit(KEY_CREATE_GROUP, userId, properties.getCreateGroupDailyLimit());
    }

    public boolean canSendMessage(String userId){
        Duration ttl = Duration.ofMinutes(1);
        return checkWindowLimit(KEY_SEND_MESSAGE, userId, ttl, properties.getMessagePerMinuteLimit());
    }

    public boolean canUpload(String userId){
        return checkDailyLimit(KEY_UPLOAD, userId, properties.getUploadDailyLimit());
    }

    public boolean canResetPassword(String userId){
        return checkDailyLimit(KEY_PASSWORD_RESET, userId, properties.getPasswordResetDailyLimit());
    }

    private boolean checkDailyLimit(String keyPrefix, String userId, long maxCount) {
        String key = keyPrefix + userId;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, getSecondsUntilMidnight(), TimeUnit.SECONDS);
        }
        return count != null && count <= maxCount;
    }

    private boolean checkWindowLimit(String keyPrefix, String userId, Duration ttl, long maxCount) {
        String key = keyPrefix + userId;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, ttl);
        }
        return count != null && count <= maxCount;
    }

    private long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight).getSeconds();
    }
}
