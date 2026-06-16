package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.dto.domain.Connection;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.FetchConnectionsRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.FetchConnectionsResponse;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FetchConnectionsRequestHandler implements BaseRequestHandler<FetchConnectionsRequest> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;


    @Override
    public void handle(WebSocketSession senderSession, FetchConnectionsRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        List<Connection> connections = userConnectionService.getConnectionsByStatus(senderUserId, request.getStatus());

        clientNotificationService.sendMessage(senderSession, senderUserId, new FetchConnectionsResponse(connections));
    }
}
