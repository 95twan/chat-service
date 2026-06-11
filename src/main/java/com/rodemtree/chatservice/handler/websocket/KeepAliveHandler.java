package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.KeepAlive;
import com.rodemtree.chatservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class KeepAliveHandler implements BaseRequestHandler<KeepAlive> {

    private final SessionService sessionService;


    @Override
    public void handle(WebSocketSession senderSession, KeepAlive request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        sessionService.refreshTTL(senderUserId, (String) senderSession.getAttributes().get(IdKey.HTTP_SESSION_ID.getValue()));
    }
}
