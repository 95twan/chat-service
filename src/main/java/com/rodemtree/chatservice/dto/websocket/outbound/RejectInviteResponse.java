package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import lombok.Getter;

@Getter
public class RejectInviteResponse extends BaseMessage {

    private final String username;
    private final UserConnectionStatus status;

    public RejectInviteResponse(String username, UserConnectionStatus status) {
        super(MessageType.REJECT_INVITE_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
