package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.Message;
import com.rodemtree.chatservice.dto.domain.UserId;

import java.util.List;

public record FetchMessagesResponseRecord(
        UserId userId,
        ChannelId channelId,
        List<Message> messages
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_MESSAGES_RESPONSE;
    }
}
