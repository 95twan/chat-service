package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import com.rodemtree.chatservice.dto.domain.UserId;


public record FetchMessagesRequestRecord(
        UserId userId,
        ChannelId channelId,
        MessageSeqId startMessageSeqId,
        MessageSeqId endMessageSeqId
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_MESSAGES_REQUEST;
    }
}
