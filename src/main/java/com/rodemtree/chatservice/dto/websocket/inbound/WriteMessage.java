package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import lombok.Getter;

@Getter
public class WriteMessage extends BaseRequest {

    private final ChannelId channelId;
    private final String content;

    @JsonCreator
    public WriteMessage(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("content") String content
    ) {
        super(MessageType.WRITE_MESSAGE);
        this.channelId = channelId;
        this.content = content;
    }
}
