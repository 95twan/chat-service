package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import com.rodemtree.chatservice.dto.domain.UserId;


public record EnterChannelResponseRecord(
        UserId userId,
        ChannelId channelId,
        String title,
        MessageSeqId lastReadMessageSeqId,
        MessageSeqId lastChannelMessageSeqId
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ENTER_CHANNEL_RESPONSE;
    }
}
