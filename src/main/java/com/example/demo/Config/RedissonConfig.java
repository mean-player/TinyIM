package com.example.demo.Config;

import com.example.demo.Component.RedissonProperties;
import org.springframework.context.annotation.Configuration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(RedissonProperties properties) {

        Config config = new Config();

        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setDatabase(properties.getDatabase())
                .setTimeout(properties.getTimeout());

        return Redisson.create(config);
    }
}