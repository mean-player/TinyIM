package com.example.demo.Config;


import com.example.demo.Component.SnowflakeIdUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfig {

    @Bean
    public SnowflakeIdUtils idGenerator() {
        return new SnowflakeIdUtils(1, 1); // 数据中心1，机器1
    }
}
//long uid = idGenerator.nextId();