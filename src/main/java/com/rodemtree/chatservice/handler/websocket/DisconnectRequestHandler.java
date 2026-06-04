package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.Constants;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.DisconnectRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.DisconnectResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.service.UserConnectionService;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class DisconnectRequestHandler implements BaseRequestHandler<DisconnectRequest> {

    private static final Logger log = LoggerFactory.getLogger(DisconnectRequestHandler.class);

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager webSocketSessionManager;

    @Override
    public void handle(WebSocketSession senderSession, DisconnectRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(Constants.USER_ID.getValue());
        Pair<Boolean, String> result = userConnectionService.disconnect(senderUserId, request.getUsername());
        if (result.getFirst()) {
            webSocketSessionManager.sendMessage(senderSession, new DisconnectResponse(request.getUsername(), UserConnectionStatus.DISCONNECTED));
        } else {
            String errorMessage = result.getSecond();
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.DISCONNECT_REQUEST, errorMessage));
        }
    }
}

