package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import com.rodemtree.chatservice.dto.domain.UserId;


public record JoinChannelRequestRecord(UserId userId, InviteCode inviteCode) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.JOIN_CHANNEL_REQUEST;
    }
}
