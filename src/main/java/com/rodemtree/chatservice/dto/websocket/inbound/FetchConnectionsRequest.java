package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import lombok.Getter;

@Getter
public class FetchConnectionsRequest extends BaseRequest {

    private final UserConnectionStatus status;

    @JsonCreator
    public FetchConnectionsRequest(@JsonProperty("status") UserConnectionStatus status) {
        super(MessageType.FETCH_CONNECTIONS_REQUEST);
        this.status = status;
    }

}
