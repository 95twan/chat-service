package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.ChannelEntry;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.EnterChannelRequestRecord;
import com.rodemtree.chatservice.dto.kafka.EnterChannelResponseRecord;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EnterChannelRequestRecordHandler implements BaseRecordHandler<EnterChannelRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<EnterChannelRequestRecord> recordType() {
        return EnterChannelRequestRecord.class;
    }

    @Override
    public void handleRecord(EnterChannelRequestRecord record) {
        UserId enterUserId = record.userId();

        Pair<Optional<ChannelEntry>, ResultType> result = channelService.enterChannel(enterUserId, record.channelId());

        result.getFirst().ifPresentOrElse(channelEntry -> {
            clientNotificationService.sendMessage(
                    enterUserId,
                    new EnterChannelResponseRecord(
                            enterUserId,
                            record.channelId(),
                            channelEntry.title(),
                            channelEntry.lastReadMessageSeqId(),
                            channelEntry.lastChannelMessageSeqId()
                    )
            );
        }, () -> {
            String errorMessage = result.getSecond().getMessage();
            clientNotificationService.sendError(
                    new ErrorResponseRecord(enterUserId, MessageType.ENTER_CHANNEL_REQUEST, errorMessage)
            );
        });
    }
}
