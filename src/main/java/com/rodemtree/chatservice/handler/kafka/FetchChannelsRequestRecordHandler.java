package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.FetchChannelsRequestRecord;
import com.rodemtree.chatservice.dto.kafka.FetchChannelsResponseRecord;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FetchChannelsRequestRecordHandler implements BaseRecordHandler<FetchChannelsRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<FetchChannelsRequestRecord> recordType() {
        return FetchChannelsRequestRecord.class;
    }

    @Override
    public void handleRecord(FetchChannelsRequestRecord record) {
        UserId senderUserId = record.userId();
        List<Channel> channels = channelService.getChannels(senderUserId);

        clientNotificationService.sendMessage(senderUserId, new FetchChannelsResponseRecord(senderUserId, channels));
    }
}
