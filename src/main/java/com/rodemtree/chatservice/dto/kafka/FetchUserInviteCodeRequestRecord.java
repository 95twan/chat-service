package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;

public record FetchUserInviteCodeRequestRecord(UserId userId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_USER_INVITE_CODE_REQUEST;
    }
}
