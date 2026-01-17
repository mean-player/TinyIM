package com.example.demo.Component;




import com.example.demo.DTO.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import java.security.Principal;
import java.util.Map;

@Component
@Slf4j
public class UserHandshakeHandler extends DefaultHandshakeHandler {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String uuid = (String) attributes.get("uuid");
        String userId = (String) attributes.get("userId");
        String principalName = uuid != null ? uuid : userId;
        log.info("WS principal bound principalName={}, userId={}", principalName, userId);
        return new UserPrincipal(principalName);
    }
}
