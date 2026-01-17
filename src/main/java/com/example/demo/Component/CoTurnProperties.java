package com.example.demo.Component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "coturn")
@Component
@Data
public class CoTurnProperties {
    private String host;
    private String secretKey;
}
