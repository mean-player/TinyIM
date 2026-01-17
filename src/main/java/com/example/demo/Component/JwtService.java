package com.example.demo.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.time.Duration;
import java.util.Date;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;


    private Key KEY;
    private long EXPIRATION_TIME;
    private long EXPIRATION_DAYS;

    @PostConstruct
    public void init(){
        this.KEY = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
        Integer minutes = jwtProperties.getAccessExpireMinutes();
        Integer days = jwtProperties.getRefreshExpireDays();
        if (minutes == null || minutes <= 0) {
            minutes = 60;
        }
        this.EXPIRATION_TIME = Duration.ofMinutes(minutes).toMillis();
        this.EXPIRATION_DAYS = Duration.ofDays(days).toMillis();
        log.info("JWT initialized, expireMinutes={}, expirationMs={}", minutes, EXPIRATION_TIME);
    }

    public String generateAccessToken(Long userId, String account) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("account", account)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId){
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_DAYS))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserId(String token) {
        return parseToken(token).getSubject();
    }
}
