package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import lombok.Getter;

@Getter
public class QuitChannelRequest extends BaseRequest {

    private final ChannelId channelId;

    @JsonCreator
    public QuitChannelRequest(
            @JsonProperty("channelId") ChannelId channelId
    ) {
        super(MessageType.QUIT_CHANNEL_REQUEST);
        this.channelId = channelId;
    }
}
