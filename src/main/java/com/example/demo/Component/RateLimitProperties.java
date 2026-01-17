package com.example.demo.Component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "limit")
@Component
@Data
public class RateLimitProperties {
    private int avatarDailyLimit = 3;
    private int nicknameCooldownMinutes = 10;
    private int signatureCooldownMinutes = 1;
    private int applicationDailyLimit = 100;
    private int createGroupDailyLimit = 20;
    private int messagePerMinuteLimit = 100;
    private int uploadDailyLimit = 100;
    private int passwordResetDailyLimit = 5;
}

