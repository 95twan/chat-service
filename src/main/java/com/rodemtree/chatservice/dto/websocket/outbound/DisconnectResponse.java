package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import lombok.Getter;

@Getter
public class DisconnectResponse extends BaseMessage {

    private final String username;
    private final UserConnectionStatus status;

    public DisconnectResponse(String username, UserConnectionStatus status) {
        super(MessageType.DISCONNECT_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
