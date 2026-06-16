package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import lombok.Getter;

@Getter
public class JoinChannelNotification extends BaseMessage {

    private final ChannelId channelId;
    private final String title;

    public JoinChannelNotification(ChannelId channelId, String title) {
        super(MessageType.NOTIFY_JOIN_CHANNEL);
        this.channelId = channelId;
        this.title = title;
    }
}
