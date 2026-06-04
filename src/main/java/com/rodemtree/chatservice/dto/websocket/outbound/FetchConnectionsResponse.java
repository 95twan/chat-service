package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.Connection;
import lombok.Getter;

import java.util.List;

@Getter
public class FetchConnectionsResponse extends BaseMessage {

    private final List<Connection> connections;

    public FetchConnectionsResponse(List<Connection> connections) {
        super(MessageType.FETCH_CONNECTIONS_RESPONSE);
        this.connections = connections;
    }
}
