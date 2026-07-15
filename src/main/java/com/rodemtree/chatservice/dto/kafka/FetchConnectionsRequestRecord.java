package com.rodemtree.chatservice.dto.kafka;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;


public record FetchConnectionsRequestRecord(UserId userId, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_CONNECTIONS_REQUEST;
    }
}
