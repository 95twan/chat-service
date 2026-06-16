package com.rodemtree.chatservice.dto.websocket.outbound;

import lombok.Getter;

@Getter
public abstract class BaseMessage {
    private final String type;

    public BaseMessage(String type) {
        this.type = type;
    }
}
