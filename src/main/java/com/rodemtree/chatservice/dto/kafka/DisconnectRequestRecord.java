package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;


public record DisconnectRequestRecord(UserId userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.DISCONNECT_REQUEST;
    }
}
