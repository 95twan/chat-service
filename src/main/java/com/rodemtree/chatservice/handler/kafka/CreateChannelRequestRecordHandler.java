package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.CreateChannelRequestRecord;
import com.rodemtree.chatservice.dto.kafka.CreateChannelResponseRecord;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.dto.kafka.JoinChannelNotificationRecord;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class CreateChannelRequestRecordHandler implements BaseRecordHandler<CreateChannelRequestRecord> {

    private final ChannelService channelService;
    private final UserService userService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<CreateChannelRequestRecord> recordType() {
        return CreateChannelRequestRecord.class;
    }

    @Override
    public void handleRecord(CreateChannelRequestRecord record) {
        UserId creatorUserId = record.userId();

        List<UserId> participantIds = userService.getUserIds(record.participantUsernames());

        if (participantIds.isEmpty()) {
            clientNotificationService.sendError(new ErrorResponseRecord(creatorUserId, MessageType.CREATE_CHANNEL_REQUEST, ResultType.NOT_FOUND.getMessage()));
            return;
        }

        Pair<Optional<Channel>, ResultType> result;
        try {
            result = channelService.createChannel(creatorUserId, participantIds, record.title());
        } catch (Exception ex) {
            clientNotificationService.sendError(new ErrorResponseRecord(creatorUserId, MessageType.CREATE_CHANNEL_REQUEST, ResultType.FAILED.getMessage()));
            return;
        }

        if (result.getFirst().isEmpty()) {
            String errorMessage = result.getSecond().getMessage();
            clientNotificationService.sendError(new ErrorResponseRecord(creatorUserId, MessageType.CREATE_CHANNEL_REQUEST, errorMessage));
            return;
        }

        Channel channel = result.getFirst().get();

        clientNotificationService.sendMessage(creatorUserId, new CreateChannelResponseRecord(creatorUserId, channel.channelId(), channel.title()));

        participantIds.forEach(partnerUserId ->
                CompletableFuture.runAsync(() ->
                        clientNotificationService.sendMessage(partnerUserId, new JoinChannelNotificationRecord(partnerUserId, channel.channelId(), channel.title()))
                )
        );
    }
}
