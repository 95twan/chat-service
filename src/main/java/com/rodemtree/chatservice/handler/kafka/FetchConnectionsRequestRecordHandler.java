package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.dto.domain.Connection;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.FetchConnectionsRequestRecord;
import com.rodemtree.chatservice.dto.kafka.FetchConnectionsResponseRecord;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FetchConnectionsRequestRecordHandler implements BaseRecordHandler<FetchConnectionsRequestRecord> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Class<FetchConnectionsRequestRecord> recordType() {
        return FetchConnectionsRequestRecord.class;
    }

    @Override
    public void handleRecord(FetchConnectionsRequestRecord record) {
        UserId senderUserId = record.userId();
        List<Connection> connections = userConnectionService.getConnectionsByStatus(senderUserId, record.status());

        clientNotificationService.sendMessage(senderUserId, new FetchConnectionsResponseRecord(senderUserId, connections));
    }
}
