package com.rodemtree.chatservice.dto.kafka.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;

public record CreateChannelResponseRecord(UserId userId, ChannelId channelId, String title) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.CREATE_CHANNEL_RESPONSE;
    }
}
