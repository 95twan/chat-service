package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.dto.domain.Connection;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.FetchConnectionsRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.FetchConnectionsResponse;
import com.rodemtree.chatservice.service.UserConnectionService;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FetchConnectionsRequestHandler implements BaseRequestHandler<FetchConnectionsRequest> {

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager webSocketSessionManager;


    @Override
    public void handle(WebSocketSession senderSession, FetchConnectionsRequest request) {
        UserId userId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        List<Connection> connections = userConnectionService.getConnectionsByStatus(userId, request.getStatus());

        webSocketSessionManager.sendMessage(senderSession, new FetchConnectionsResponse(connections));
    }
}
