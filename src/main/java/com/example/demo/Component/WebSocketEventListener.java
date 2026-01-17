package com.example.demo.Component;


import com.example.demo.Service.ReadRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final StringRedisTemplate redis;
    private final JwtService jwtService;
    private final ReadRecordService readRecordService;

    //处理连接事件
    @EventListener
    public void handleConnectEvent(SessionConnectEvent event) {
        log.info("1");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // 1. 解析 Token 获取 UserId
        String token = accessor.getFirstNativeHeader("Authorization");
        String userId = null;

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = jwtService.getUserId(token);
            } catch (Exception e) {
                log.error("WebSocket连接鉴权失败: {}", e.getMessage());
                return; // 或者抛出异常断开连接
            }
        }

        if (userId != null) {
            log.info("{}",userId);
            // 2. 关键步骤：将 userId 放入 Session 属性中，以便断开连接时使用
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                sessionAttributes.put("userId", userId);
            }

            // 3. Redis 操作：设备数 +1
            String connKey = "user:conn:" + userId;
            redis.opsForValue().increment(connKey);

            readRecordService.cacheUserReadRecords(Long.valueOf(userId));


            // 4. Redis 操作：标记在线
            // 注意：这里设置了过期时间，建议配合心跳机制续期，否则300s后用户还在连接但Redis key没了
            redis.opsForValue().set("user:online:" + userId, "1", 300, TimeUnit.SECONDS);

            log.info("User {} connected. Device count incremented.", userId);
        }
    }


    //处理断开连接事件
    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // 1. 从 Session 属性中直接获取 UserId (不需要重新解析 Token)
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        String userId = (sessionAttributes != null) ? (String) sessionAttributes.get("userId") : null;

        if (userId != null) {
            String connKey = "user:conn:" + userId;
            String onlineKey = "user:online:" + userId;

            // 2. Redis 操作：设备数 -1
            Long currentCount = redis.opsForValue().decrement(connKey);

            // 3. 判断是否需要移除在线状态
            // 如果 currentCount 为 null 或者 <= 0，说明这是最后一个设备断开
            if (currentCount == null || currentCount <= 0) {
                readRecordService.flushReadRecordToDB(Long.valueOf(userId));
                redis.delete(onlineKey);
                redis.delete(connKey);

                log.info("User {} 下线. All devices offline.", userId);
            } else {
                log.info("User {} device disconnected. Remaining devices: {}", userId, currentCount);
            }
        }
    }
}
