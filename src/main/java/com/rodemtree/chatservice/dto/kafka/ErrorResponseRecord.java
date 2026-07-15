package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;


public record ErrorResponseRecord(
        UserId userId,
        String messageType,
        String message
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ERROR;
    }
}
