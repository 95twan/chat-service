package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.DisconnectRequestRecord;
import com.rodemtree.chatservice.dto.kafka.DisconnectResponseRecord;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DisconnectRequestRecordHandler implements BaseRecordHandler<DisconnectRequestRecord> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<DisconnectRequestRecord> recordType() {
        return DisconnectRequestRecord.class;
    }

    @Override
    public void handleRecord(DisconnectRequestRecord record) {
        UserId senderUserId = record.userId();
        Pair<Boolean, String> result = userConnectionService.disconnect(senderUserId, record.username());
        if (result.getFirst()) {
            clientNotificationService.sendMessage(senderUserId, new DisconnectResponseRecord(senderUserId, record.username(), UserConnectionStatus.DISCONNECTED));
        } else {
            String errorMessage = result.getSecond();
            clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, MessageType.DISCONNECT_REQUEST, errorMessage));
        }
    }
}

