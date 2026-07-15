package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;

public record JoinChannelResponseRecord(
        UserId userId,
        ChannelId channelId,
        String title
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.JOIN_CHANNEL_RESPONSE;
    }
}
