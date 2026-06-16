package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import lombok.Getter;

@Getter
public class FetchChannelInviteCodeResponse extends BaseMessage {

    private final ChannelId channelId;
    private final InviteCode inviteCode;

    public FetchChannelInviteCodeResponse(ChannelId channelId, InviteCode inviteCode) {
        super(MessageType.FETCH_CHANNEL_INVITE_CODE_RESPONSE);
        this.channelId = channelId;
        this.inviteCode = inviteCode;
    }
}
