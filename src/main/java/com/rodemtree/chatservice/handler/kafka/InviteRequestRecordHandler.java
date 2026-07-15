package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.dto.kafka.InviteNotificationRecord;
import com.rodemtree.chatservice.dto.kafka.InviteRequestRecord;
import com.rodemtree.chatservice.dto.kafka.InviteResponseRecord;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InviteRequestRecordHandler implements BaseRecordHandler<InviteRequestRecord> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<InviteRequestRecord> recordType() {
        return InviteRequestRecord.class;
    }

    @Override
    public void handleRecord(InviteRequestRecord record) {
        UserId inviterUserId = record.userId();
        Pair<Optional<UserId>, String> result = userConnectionService.invite(inviterUserId, record.userInviteCode());
        result.getFirst().ifPresentOrElse(partnerUserId -> {
            String inviterUsername = result.getSecond();
            clientNotificationService.sendMessage(inviterUserId, new InviteResponseRecord(inviterUserId, record.userInviteCode(), UserConnectionStatus.PENDING));
            clientNotificationService.sendMessage(partnerUserId, new InviteNotificationRecord(partnerUserId, inviterUsername));
        }, () -> {
            String errorMessage = result.getSecond();
            clientNotificationService.sendError(new ErrorResponseRecord(inviterUserId, MessageType.INVITE_REQUEST, errorMessage));
        });

    }
}
