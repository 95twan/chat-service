package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import lombok.Getter;

@Getter
public class FetchChannelInviteCodeRequest extends BaseRequest {

    private final ChannelId channelId;

    @JsonCreator
    public FetchChannelInviteCodeRequest(@JsonProperty("channelId") ChannelId channelId) {
        super(MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST);
        this.channelId = channelId;
    }
}
