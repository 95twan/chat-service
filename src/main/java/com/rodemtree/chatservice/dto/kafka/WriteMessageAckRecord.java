package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import com.rodemtree.chatservice.dto.domain.UserId;

public record WriteMessageAckRecord(
        UserId userId,
        Long serial,
        MessageSeqId messageSeqId
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.WRITE_MESSAGE_ACK;
    }
}
