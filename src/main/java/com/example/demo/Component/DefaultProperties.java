package com.example.demo.Component;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "default")
@Component
@Data
public class DefaultProperties {
    private String userAvatar;
    private String groupAvatar;
}
