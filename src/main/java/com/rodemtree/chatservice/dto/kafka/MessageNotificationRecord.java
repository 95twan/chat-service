package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import com.rodemtree.chatservice.dto.domain.UserId;

import java.util.List;

public record MessageNotificationRecord(
        UserId userId,
        ChannelId channelId,
        MessageSeqId messageSeqId,
        String username,
        String content,
        List<UserId> participantIds
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.NOTIFY_MESSAGE;
    }
}
