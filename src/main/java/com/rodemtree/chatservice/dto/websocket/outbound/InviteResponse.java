package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import lombok.Getter;

@Getter
public class InviteResponse extends BaseMessage {

    private final InviteCode inviteCode;
    private final UserConnectionStatus status;

    public InviteResponse(InviteCode inviteCode, UserConnectionStatus status) {
        super(MessageType.INVITE_RESPONSE);
        this.inviteCode = inviteCode;
        this.status = status;
    }
}
