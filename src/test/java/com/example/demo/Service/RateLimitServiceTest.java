package com.example.demo.Service;

import com.example.demo.Component.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitServiceTest {

    private StringRedisTemplate redis;
    private RateLimitProperties properties;
    private RateLimitService rateLimitService;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        properties = new RateLimitProperties();
        rateLimitService = new RateLimitService(redis, properties);
        valueOperations = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void canChangeAvatarWithinDailyLimit() {
        when(valueOperations.increment("limit:avatar:u1")).thenReturn(1L, 2L, 3L, 4L);

        assertTrue(rateLimitService.canChangeAvatar("u1"));
        assertTrue(rateLimitService.canChangeAvatar("u1"));
        assertTrue(rateLimitService.canChangeAvatar("u1"));
        assertFalse(rateLimitService.canChangeAvatar("u1"));

        verify(redis, times(1)).expire(eq("limit:avatar:u1"), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void canChangeNicknameRespectsCooldown() {
        when(redis.hasKey("limit:nickname:u1")).thenReturn(false, true);

        assertTrue(rateLimitService.canChangeNickname("u1"));
        assertFalse(rateLimitService.canChangeNickname("u1"));

        verify(valueOperations).set(eq("limit:nickname:u1"), eq("1"),
                eq((long) properties.getNicknameCooldownMinutes()), eq(TimeUnit.MINUTES));
    }

    @Test
    void canSendMessageRespectsWindowLimit() {
        when(valueOperations.increment("limit:sendmessage:u1")).thenReturn(1L, 100L, 101L);

        assertTrue(rateLimitService.canSendMessage("u1"));
        assertTrue(rateLimitService.canSendMessage("u1"));
        assertFalse(rateLimitService.canSendMessage("u1"));

        verify(redis, times(1)).expire(eq("limit:sendmessage:u1"), any(Duration.class));
    }
}

