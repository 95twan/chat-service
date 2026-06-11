package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.rodemtree.chatservice.constant.MessageType;

public class KeepAlive extends BaseRequest {

    @JsonCreator
    public KeepAlive() {
        super(MessageType.KEEP_ALIVE);
    }
}
