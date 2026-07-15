package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;

public record LeaveChannelResponseRecord(UserId userId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.LEAVE_CHANNEL_RESPONSE;
    }
}
