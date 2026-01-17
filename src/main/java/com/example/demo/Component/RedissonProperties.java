package com.example.demo.Component;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {
    private String host;
    private int port;
    private int database;
    private String password;
    private int timeout;
}