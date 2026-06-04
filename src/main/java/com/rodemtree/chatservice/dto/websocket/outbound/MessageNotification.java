package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class MessageNotification extends BaseMessage {

    private final String username;
    private final String content;


    public MessageNotification(String username, String content) {
        super(MessageType.NOTIFY_MESSAGE);
        this.username = username;
        this.content = content;
    }
}
