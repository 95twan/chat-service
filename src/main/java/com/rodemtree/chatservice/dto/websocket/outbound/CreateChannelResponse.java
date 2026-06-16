package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import lombok.Getter;

@Getter
public class CreateChannelResponse extends BaseMessage {

    private final ChannelId channelId;
    private final String title;

    public CreateChannelResponse(ChannelId channelId, String title) {
        super(MessageType.CREATE_CHANNEL_RESPONSE);
        this.channelId = channelId;
        this.title = title;
    }
}
