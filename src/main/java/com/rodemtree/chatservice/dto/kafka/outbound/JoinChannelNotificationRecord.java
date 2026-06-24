package com.rodemtree.chatservice.dto.kafka.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;


public record JoinChannelNotificationRecord(UserId userId, ChannelId channelId, String title) implements RecordInterface {
    @Override
    public String type() {
        return MessageType.NOTIFY_JOIN_CHANNEL;
    }
}
