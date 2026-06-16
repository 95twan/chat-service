package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.dto.websocket.inbound.BaseRequest;
import org.springframework.web.socket.WebSocketSession;

public interface BaseRequestHandler<T extends BaseRequest> {
    void handle(WebSocketSession webSocketSession, T request);
}
