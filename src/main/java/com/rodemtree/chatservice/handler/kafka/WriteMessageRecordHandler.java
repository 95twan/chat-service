package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.dto.kafka.WriteMessageRecord;
import com.rodemtree.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WriteMessageRecordHandler implements BaseRecordHandler<WriteMessageRecord> {

    private final MessageService messageService;

    @Override
    public Class<WriteMessageRecord> recordType() {
        return WriteMessageRecord.class;
    }

    @Override
    public void handleRecord(WriteMessageRecord record) {
        messageService.sendMessage(record);
    }
}
