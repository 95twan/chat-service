package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import lombok.Getter;

@Getter
public class FetchUserInviteCodeResponse extends BaseMessage {

    private final InviteCode inviteCode;

    public FetchUserInviteCodeResponse(InviteCode inviteCode) {
        super(MessageType.FETCH_USER_INVITE_CODE_RESPONSE);
        this.inviteCode = inviteCode;
    }

}
