package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class AcceptInviteResponse extends BaseMessage {

    private final String username;

    public AcceptInviteResponse(String username) {
        super(MessageType.ACCEPT_INVITE_RESPONSE);
        this.username = username;
    }
}
