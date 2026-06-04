package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.Constants;
import com.rodemtree.chatservice.dto.websocket.inbound.KeepAliveRequest;
import com.rodemtree.chatservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class KeepAliveRequestHandler implements BaseRequestHandler<KeepAliveRequest> {

    private final SessionService sessionService;


    @Override
    public void handle(WebSocketSession senderSession, KeepAliveRequest request) {
        sessionService.refreshTTL((String) senderSession.getAttributes().get(Constants.HTTP_SESSION_ID.getValue()));
    }
}
