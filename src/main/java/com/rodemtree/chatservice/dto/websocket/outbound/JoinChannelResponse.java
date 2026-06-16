package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import lombok.Getter;

@Getter
public class JoinChannelResponse extends BaseMessage {

    private final ChannelId channelId;
    private final String title;

    public JoinChannelResponse(ChannelId channelId, String title) {
        super(MessageType.JOIN_CHANNEL_RESPONSE);
        this.channelId = channelId;
        this.title = title;
    }
}
