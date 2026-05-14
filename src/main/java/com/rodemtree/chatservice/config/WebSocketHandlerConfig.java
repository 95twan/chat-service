package com.rodemtree.chatservice.config;

import com.rodemtree.chatservice.auth.WebSocketHttpSessionHandshakeInterceptor;
import com.rodemtree.chatservice.handler.MessageHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketHandlerConfig implements WebSocketConfigurer {

    private final MessageHandler messageHandler;
    private final WebSocketHttpSessionHandshakeInterceptor webSocketHttpSessionHandshakeInterceptor;

    public WebSocketHandlerConfig(MessageHandler messageHandler, WebSocketHttpSessionHandshakeInterceptor webSocketHttpSessionHandshakeInterceptor) {
        this.messageHandler = messageHandler;
        this.webSocketHttpSessionHandshakeInterceptor = webSocketHttpSessionHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageHandler, "/ws/v1/message")
                .addInterceptors(webSocketHttpSessionHandshakeInterceptor);
    }
}
