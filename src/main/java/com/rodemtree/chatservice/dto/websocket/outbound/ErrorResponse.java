package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class ErrorResponse extends BaseMessage {

    private final String messageType;
    private final String message;

    public ErrorResponse(String messageType, String message) {
        super(MessageType.ERROR);
        this.messageType = messageType;
        this.message = message;
    }
}
