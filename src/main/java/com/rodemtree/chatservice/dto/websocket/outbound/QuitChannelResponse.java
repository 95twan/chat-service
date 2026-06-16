package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import lombok.Getter;

@Getter
public class QuitChannelResponse extends BaseMessage {

    private final ChannelId channelId;

    public QuitChannelResponse(ChannelId channelId) {
        super(MessageType.QUIT_CHANNEL_RESPONSE);
        this.channelId = channelId;
    }
}
