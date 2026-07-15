package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import com.rodemtree.chatservice.dto.domain.UserId;

public record FetchUserInviteCodeResponseRecord(
        UserId userId,
        InviteCode inviteCode
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_USER_INVITE_CODE_RESPONSE;
    }
}
