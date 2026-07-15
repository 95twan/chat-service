package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.UserId;

import java.util.List;

public record FetchChannelsResponseRecord(
        UserId userId,
        List<Channel> channels
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_CHANNELS_RESPONSE;
    }
}
