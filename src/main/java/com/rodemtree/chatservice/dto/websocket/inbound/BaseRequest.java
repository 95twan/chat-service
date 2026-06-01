package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rodemtree.chatservice.constant.MessageType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FetchUserInviteCodeRequest.class, name = MessageType.FETCH_USER_INVITE_CODE_REQUEST),
        @JsonSubTypes.Type(value = FetchConnectionsRequest.class, name = MessageType.FETCH_CONNECTIONS_REQUEST),
        @JsonSubTypes.Type(value = InviteRequest.class, name = MessageType.INVITE_REQUEST),
        @JsonSubTypes.Type(value = AcceptRequest.class, name = MessageType.ACCEPT_REQUEST),
        @JsonSubTypes.Type(value = RejectInviteRequest.class, name = MessageType.REJECT_INVITE_REQUEST),
        @JsonSubTypes.Type(value = WriteMessageRequest.class, name = MessageType.WRITE_MESSAGE),
        @JsonSubTypes.Type(value = KeepAliveRequest.class, name = MessageType.KEEP_ALIVE)
})
public abstract class BaseRequest {
    private final String type;

    public BaseRequest(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
