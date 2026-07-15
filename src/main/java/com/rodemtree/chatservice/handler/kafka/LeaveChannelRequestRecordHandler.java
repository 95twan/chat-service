package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.ErrorResponseRecord;
import com.rodemtree.chatservice.dto.kafka.LeaveChannelRequestRecord;
import com.rodemtree.chatservice.dto.kafka.LeaveChannelResponseRecord;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaveChannelRequestRecordHandler implements BaseRecordHandler<LeaveChannelRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<LeaveChannelRequestRecord> recordType() {
        return LeaveChannelRequestRecord.class;
    }

    @Override
    public void handleRecord(LeaveChannelRequestRecord record) {
        UserId leaveUserId = record.userId();

        if (channelService.leaveChannel(leaveUserId)) {
            clientNotificationService.sendMessage(leaveUserId, new LeaveChannelResponseRecord(leaveUserId));
        } else {
            clientNotificationService.sendError(new ErrorResponseRecord(leaveUserId, MessageType.LEAVE_CHANNEL_REQUEST, "Failed to leave channel"));
        }
    }
}
