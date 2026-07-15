package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.dto.kafka.QuitChannelRequestRecord;
import com.rodemtree.chatservice.dto.kafka.QuitChannelResponseRecord;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuitChannelRequestRecordHandler implements BaseRecordHandler<QuitChannelRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;


    @Override
    public Class<QuitChannelRequestRecord> recordType() {
        return QuitChannelRequestRecord.class;
    }

    @Override
    public void handleRecord(QuitChannelRequestRecord record) {
        UserId quitUserId = record.userId();

        ResultType result;

        try {
            result = channelService.quitChannel(quitUserId, record.channelId());
        } catch (Exception ex) {
            clientNotificationService.sendError(new ErrorResponseRecord(quitUserId, MessageType.QUIT_CHANNEL_REQUEST, ResultType.FAILED.getMessage()));
            return;
        }

        if (result == ResultType.SUCCESS) {
            clientNotificationService.sendMessage(quitUserId, new QuitChannelResponseRecord(quitUserId, record.channelId()));
        } else {
            clientNotificationService.sendError(new ErrorResponseRecord(quitUserId, MessageType.QUIT_CHANNEL_REQUEST, result.getMessage()));
        }
    }
}
