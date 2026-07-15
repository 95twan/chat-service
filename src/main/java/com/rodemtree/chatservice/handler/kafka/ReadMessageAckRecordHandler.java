package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.ReadMessageAckRecord;
import com.rodemtree.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadMessageAckRecordHandler implements BaseRecordHandler<ReadMessageAckRecord> {

    private final MessageService messageService;

    @Override
    public Class<ReadMessageAckRecord> recordType() {
        return ReadMessageAckRecord.class;
    }

    @Override
    public void handleRecord(ReadMessageAckRecord record) {
        UserId senderUserId = record.userId();
        ChannelId channelId = record.channelId();
        messageService.updateLastReadMessageSeq(senderUserId, channelId, record.messageSeqId());
    }
}
