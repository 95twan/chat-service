package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.dto.kafka.RejectInviteRequestRecord;
import com.rodemtree.chatservice.dto.kafka.RejectInviteResponseRecord;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RejectInviteRequestRecordHandler implements BaseRecordHandler<RejectInviteRequestRecord> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;


    @Override
    public Class<RejectInviteRequestRecord> recordType() {
        return RejectInviteRequestRecord.class;
    }

    @Override
    public void handleRecord(RejectInviteRequestRecord record) {
        UserId rejectorUserId = record.userId();
        Pair<Boolean, String> result = userConnectionService.rejectInvite(rejectorUserId, record.username());

        if (result.getFirst()) {
            clientNotificationService.sendMessage(rejectorUserId, new RejectInviteResponseRecord(rejectorUserId, record.username(), UserConnectionStatus.REJECTED));
        } else {
            String errorMessage = result.getSecond();
            clientNotificationService.sendError(new ErrorResponseRecord(rejectorUserId, MessageType.REJECT_INVITE_REQUEST, errorMessage));
        }
    }
}
