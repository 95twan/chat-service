package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;

public record FetchChannelsRequestRecord(UserId userId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_CHANNELS_REQUEST;
    }
}
