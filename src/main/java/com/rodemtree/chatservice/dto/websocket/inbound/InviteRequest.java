package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import lombok.Getter;

@Getter
public class InviteRequest extends BaseRequest {

    private final InviteCode userInviteCode;

    @JsonCreator
    public InviteRequest(
            @JsonProperty("userInviteCode") InviteCode userInviteCode
    ) {
        super(MessageType.INVITE_REQUEST);
        this.userInviteCode = userInviteCode;
    }
}
