package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.AcceptInviteNotificationRecord;
import com.rodemtree.chatservice.dto.kafka.AcceptInviteRequestRecord;
import com.rodemtree.chatservice.dto.kafka.AcceptInviteResponseRecord;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AcceptInviteRequestRecordHandler implements BaseRecordHandler<AcceptInviteRequestRecord> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<AcceptInviteRequestRecord> recordType() {
        return AcceptInviteRequestRecord.class;
    }

    @Override
    public void handleRecord(AcceptInviteRequestRecord record) {
        UserId acceptorUserId = record.userId();
        Pair<Optional<UserId>, String> result = userConnectionService.acceptInvite(acceptorUserId, record.username());

        result.getFirst().ifPresentOrElse(inviterUserId -> {
            clientNotificationService.sendMessage(acceptorUserId, new AcceptInviteResponseRecord(acceptorUserId, record.username()));
            String acceptorUsername = result.getSecond();
            clientNotificationService.sendMessage(inviterUserId, new AcceptInviteNotificationRecord(inviterUserId, acceptorUsername));
        }, () -> {
            String errorMessage = result.getSecond();
            clientNotificationService.sendError(new ErrorResponseRecord(acceptorUserId, MessageType.ACCEPT_INVITE_REQUEST, errorMessage));
        });
    }
}
