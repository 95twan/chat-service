package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class AcceptInviteNotification extends BaseMessage {

    private final String username;

    public AcceptInviteNotification(String username) {
        super(MessageType.NOTIFY_ACCEPT_INVITE);
        this.username = username;
    }
}
