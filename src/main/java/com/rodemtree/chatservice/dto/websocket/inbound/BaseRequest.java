package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FetchUserInviteCodeRequest.class, name = MessageType.FETCH_USER_INVITE_CODE_REQUEST),
        @JsonSubTypes.Type(value = FetchConnectionsRequest.class, name = MessageType.FETCH_CONNECTIONS_REQUEST),
        @JsonSubTypes.Type(value = InviteRequest.class, name = MessageType.INVITE_REQUEST),
        @JsonSubTypes.Type(value = AcceptInviteRequest.class, name = MessageType.ACCEPT_INVITE_REQUEST),
        @JsonSubTypes.Type(value = RejectInviteRequest.class, name = MessageType.REJECT_INVITE_REQUEST),
        @JsonSubTypes.Type(value = DisconnectRequest.class, name = MessageType.DISCONNECT_REQUEST),
        @JsonSubTypes.Type(value = FetchChannelInviteCodeRequest.class, name = MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST),
        @JsonSubTypes.Type(value = FetchChannelsRequest.class, name = MessageType.FETCH_CHANNELS_REQUEST),
        @JsonSubTypes.Type(value = CreateChannelRequest.class, name = MessageType.CREATE_CHANNEL_REQUEST),
        @JsonSubTypes.Type(value = EnterChannelRequest.class, name = MessageType.ENTER_CHANNEL_REQUEST),
        @JsonSubTypes.Type(value = JoinChannelRequest.class, name = MessageType.JOIN_CHANNEL_REQUEST),
        @JsonSubTypes.Type(value = LeaveChannelRequest.class, name = MessageType.LEAVE_CHANNEL_REQUEST),
        @JsonSubTypes.Type(value = QuitChannelRequest.class, name = MessageType.QUIT_CHANNEL_REQUEST),
        @JsonSubTypes.Type(value = WriteMessage.class, name = MessageType.WRITE_MESSAGE),
        @JsonSubTypes.Type(value = ReadMessageAck.class, name = MessageType.READ_MESSAGE_ACK),
        @JsonSubTypes.Type(value = FetchMessagesRequest.class, name = MessageType.FETCH_MESSAGES_REQUEST),
        @JsonSubTypes.Type(value = KeepAlive.class, name = MessageType.KEEP_ALIVE)
})
public abstract class BaseRequest {
    private final String type;

    public BaseRequest(String type) {
        this.type = type;
    }
}
