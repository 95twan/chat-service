package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import com.rodemtree.chatservice.dto.domain.UserId;

public record InviteResponseRecord(UserId userId, InviteCode inviteCode, UserConnectionStatus status) implements RecordInterface {
    @Override
    public String type() {
        return MessageType.INVITE_RESPONSE;
    }
}
