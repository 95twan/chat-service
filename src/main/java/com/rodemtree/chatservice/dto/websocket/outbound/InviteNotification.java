package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class InviteNotification extends BaseMessage {

    private final String username;

    public InviteNotification(String username) {
        super(MessageType.ASK_INVITE);
        this.username = username;
    }
}
