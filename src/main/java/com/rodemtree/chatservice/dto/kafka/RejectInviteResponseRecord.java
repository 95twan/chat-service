package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;

public record RejectInviteResponseRecord(UserId userId, String username, UserConnectionStatus status) implements RecordInterface {
    @Override
    public String type() {
        return MessageType.REJECT_INVITE_RESPONSE;
    }
}
