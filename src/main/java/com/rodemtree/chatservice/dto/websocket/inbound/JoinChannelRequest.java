package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import lombok.Getter;

@Getter
public class JoinChannelRequest extends BaseRequest {

    private final InviteCode inviteCode;

    @JsonCreator
    public JoinChannelRequest(
            @JsonProperty("inviteCode") InviteCode inviteCode
    ) {
        super(MessageType.JOIN_CHANNEL_REQUEST);
        this.inviteCode = inviteCode;
    }
}
