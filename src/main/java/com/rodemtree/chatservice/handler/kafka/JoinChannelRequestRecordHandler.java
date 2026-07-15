package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.dto.kafka.JoinChannelRequestRecord;
import com.rodemtree.chatservice.dto.kafka.JoinChannelResponseRecord;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JoinChannelRequestRecordHandler implements BaseRecordHandler<JoinChannelRequestRecord> {

    private static final Logger log = LoggerFactory.getLogger(JoinChannelRequestRecordHandler.class);

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<JoinChannelRequestRecord> recordType() {
        return JoinChannelRequestRecord.class;
    }

    @Override
    public void handleRecord(JoinChannelRequestRecord record) {
        UserId joinUserId = record.userId();

        Pair<Optional<Channel>, ResultType> result;

        try {
            result = channelService.joinChannel(record.inviteCode(), joinUserId);
        } catch (Exception ex) {
            log.error("Join channel failed. cause: {}", ex.getMessage());
            clientNotificationService.sendError(new ErrorResponseRecord(joinUserId, MessageType.JOIN_CHANNEL_REQUEST, ResultType.FAILED.getMessage()));
            return;
        }

        result.getFirst().ifPresentOrElse(channel -> {
            clientNotificationService.sendMessage(joinUserId, new JoinChannelResponseRecord(joinUserId, channel.channelId(), channel.title()));
        }, () -> {
            String errorMessage = result.getSecond().getMessage();
            clientNotificationService.sendError(new ErrorResponseRecord(joinUserId, MessageType.JOIN_CHANNEL_REQUEST, errorMessage));
        });
    }
}
