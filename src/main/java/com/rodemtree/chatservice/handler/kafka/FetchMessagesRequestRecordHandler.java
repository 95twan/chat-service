package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.Message;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.dto.kafka.FetchMessagesRequestRecord;
import com.rodemtree.chatservice.dto.kafka.FetchMessagesResponseRecord;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FetchMessagesRequestRecordHandler implements BaseRecordHandler<FetchMessagesRequestRecord> {

    private final MessageService messageService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<FetchMessagesRequestRecord> recordType() {
        return FetchMessagesRequestRecord.class;
    }

    @Override
    public void handleRecord(FetchMessagesRequestRecord record) {
        UserId senderUserId = record.userId();
        ChannelId channelId = record.channelId();
        Pair<List<Message>, ResultType> result = messageService.getMessages(channelId, record.startMessageSeqId(), record.endMessageSeqId());
        if (result.getSecond() == ResultType.SUCCESS) {
            List<Message> messages = result.getFirst();
            clientNotificationService.sendMessageUsingPartitionKey(channelId, senderUserId, new FetchMessagesResponseRecord(senderUserId, channelId, messages));
        } else {

            clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, MessageType.FETCH_MESSAGES_REQUEST, result.getSecond().getMessage()));
        }
    }
}
