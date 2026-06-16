package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class RejectInviteRequest extends BaseRequest {

    private final String username;


    @JsonCreator
    public RejectInviteRequest(
            @JsonProperty("username") String username
    ) {
        super(MessageType.REJECT_INVITE_REQUEST);
        this.username = username;
    }
}
