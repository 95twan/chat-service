package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import com.rodemtree.chatservice.dto.domain.UserId;


public record ReadMessageAckRecord(UserId userId, ChannelId channelId, MessageSeqId messageSeqId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.READ_MESSAGE_ACK;
    }

}
