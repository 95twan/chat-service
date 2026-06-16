package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class AcceptInviteRequest extends BaseRequest {

    private final String username;

    @JsonCreator
    public AcceptInviteRequest(
            @JsonProperty("username") String username
    ) {
        super(MessageType.ACCEPT_INVITE_REQUEST);
        this.username = username;
    }
}
