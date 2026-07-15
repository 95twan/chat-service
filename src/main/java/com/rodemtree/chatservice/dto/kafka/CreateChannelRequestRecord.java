package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;

import java.util.List;

public record CreateChannelRequestRecord(UserId userId, String title, List<String> participantUsernames) implements RecordInterface {


    @Override
    public String type() {
        return MessageType.CREATE_CHANNEL_REQUEST;
    }
}
