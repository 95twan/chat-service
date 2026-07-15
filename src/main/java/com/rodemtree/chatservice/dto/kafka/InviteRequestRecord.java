package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import com.rodemtree.chatservice.dto.domain.UserId;


public record InviteRequestRecord(UserId userId, InviteCode userInviteCode) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.INVITE_REQUEST;
    }
}
