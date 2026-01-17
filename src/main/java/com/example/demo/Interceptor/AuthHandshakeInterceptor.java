package com.example.demo.Interceptor;


import com.example.demo.Component.JwtService;
import com.example.demo.Service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final TokenService tokenService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        URI uri = request.getURI();
        String query = uri.getQuery();
        String token = null;

        if (query != null) {
            log.info("WS handshake query={}", query);
            for (String p : query.split("&")) {
                if (p.startsWith("token=")) {
                    token = p.substring("token=".length());
                    break;
                }
            }
        }

        if (token == null) {
            log.warn("WS handshake missing token");
            return false;
        }


        //String userId = jwtService.parseToken(token).getSubject();

        String userId;
        try {
            userId = jwtService.parseToken(token).getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("WS handshake failed: token expired");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        } catch (Exception e) {
            log.warn("WS handshake failed: invalid token {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String uuid = tokenService.getUUIDByUserId(Long.valueOf(userId));

        attributes.put("userId", userId);
        if (uuid != null) {
            attributes.put("uuid", uuid);
        }
        log.info("WS handshake success userId={}, uuid={}", userId, uuid);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception ex) {}
}
