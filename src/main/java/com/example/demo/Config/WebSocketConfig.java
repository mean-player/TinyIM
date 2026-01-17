package com.example.demo.Config;



import com.example.demo.Component.UserHandshakeHandler;
import com.example.demo.Interceptor.AuthHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    private final UserHandshakeHandler userHandshakeHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(authHandshakeInterceptor)
                .setHandshakeHandler(userHandshakeHandler)
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 前缀 /app 代表客户端发送的消息走 @MessageMapping 方法
        registry.setApplicationDestinationPrefixes("/app");

        // 启用内置的简单消息代理，前缀 /user 表示点对点消息
        registry.enableSimpleBroker("/topic","/queue");
        registry.setUserDestinationPrefix("/user");
    }


    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(512 * 1024)
                .setTimeToFirstMessage(10 * 1000);
    }
}