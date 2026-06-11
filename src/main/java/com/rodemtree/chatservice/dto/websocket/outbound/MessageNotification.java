package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import lombok.Getter;

@Getter
public class MessageNotification extends BaseMessage {

    private final ChannelId channelId;
    private final String username;
    private final String content;


    public MessageNotification(ChannelId channelId, String username, String content) {
        super(MessageType.NOTIFY_MESSAGE);
        this.channelId = channelId;
        this.username = username;
        this.content = content;
    }
}
