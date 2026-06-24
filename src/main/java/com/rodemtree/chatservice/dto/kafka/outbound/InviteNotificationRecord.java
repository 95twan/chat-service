package com.rodemtree.chatservice.dto.kafka.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;

public record InviteNotificationRecord(UserId userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ASK_INVITE;
    }
}
