package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class DisconnectRequest extends BaseRequest {

    private final String username;

    @JsonCreator
    public DisconnectRequest(
            @JsonProperty("username") String username
    ) {
        super(MessageType.DISCONNECT_REQUEST);
        this.username = username;
    }

}
