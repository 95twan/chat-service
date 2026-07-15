package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.dto.kafka.FetchUserInviteCodeRequestRecord;
import com.rodemtree.chatservice.dto.kafka.FetchUserInviteCodeResponseRecord;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FetchUserInviteCodeRequestRecordHandler implements BaseRecordHandler<FetchUserInviteCodeRequestRecord> {

    private final UserService userService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<FetchUserInviteCodeRequestRecord> recordType() {
        return FetchUserInviteCodeRequestRecord.class;
    }

    @Override
    public void handleRecord(FetchUserInviteCodeRequestRecord record) {
        UserId senderUserId = record.userId();

        userService.getInviteCode(senderUserId).ifPresentOrElse(inviteCode -> {
            clientNotificationService.sendMessage(senderUserId, new FetchUserInviteCodeResponseRecord(senderUserId, inviteCode));
        }, () -> {
            clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, MessageType.FETCH_USER_INVITE_CODE_REQUEST, "Fetch user invite code failed."));
        });
    }
}
