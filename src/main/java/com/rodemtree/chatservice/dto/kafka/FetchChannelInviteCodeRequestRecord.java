package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;


public record FetchChannelInviteCodeRequestRecord(UserId userId, ChannelId channelId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST;
    }
}
