package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.Message;
import lombok.Getter;

import java.util.List;

@Getter
public class FetchMessagesResponse extends BaseMessage {

    private final ChannelId channelId;
    private final List<Message> messages;

    public FetchMessagesResponse(ChannelId channelId, List<Message> messages) {
        super(MessageType.FETCH_MESSAGES_RESPONSE);
        this.channelId = channelId;
        this.messages = messages;
    }
}
