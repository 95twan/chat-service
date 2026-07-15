package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.dto.kafka.FetchChannelInviteCodeRequestRecord;
import com.rodemtree.chatservice.dto.kafka.FetchChannelInviteCodeResponseRecord;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FetchChannelInviteCodeRequestRecordHandler implements BaseRecordHandler<FetchChannelInviteCodeRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<FetchChannelInviteCodeRequestRecord> recordType() {
        return FetchChannelInviteCodeRequestRecord.class;
    }

    @Override
    public void handleRecord(FetchChannelInviteCodeRequestRecord record) {
        UserId senderUserId = record.userId();

        if (!channelService.isJoined(senderUserId, record.channelId())) {
            clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST, "Not joined the channel."));
            return;
        }
        channelService.getChannelInviteCode(record.channelId()).ifPresentOrElse(inviteCode -> {
            clientNotificationService.sendMessage(senderUserId, new FetchChannelInviteCodeResponseRecord(senderUserId, record.channelId(), inviteCode));
        }, () -> {
            clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST, "Fetch channel invite code failed."));
        });
    }
}
