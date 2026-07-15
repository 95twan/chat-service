package com.rodemtree.chatservice.dto.kafka;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rodemtree.chatservice.constant.MessageType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FetchUserInviteCodeRequestRecord.class, name = MessageType.FETCH_USER_INVITE_CODE_REQUEST),
        @JsonSubTypes.Type(value = FetchUserInviteCodeResponseRecord.class, name = MessageType.FETCH_USER_INVITE_CODE_RESPONSE),
        @JsonSubTypes.Type(value = FetchConnectionsRequestRecord.class, name = MessageType.FETCH_CONNECTIONS_REQUEST),
        @JsonSubTypes.Type(value = FetchConnectionsResponseRecord.class, name = MessageType.FETCH_CONNECTIONS_RESPONSE),
        @JsonSubTypes.Type(value = InviteRequestRecord.class, name = MessageType.INVITE_REQUEST),
        @JsonSubTypes.Type(value = InviteResponseRecord.class, name = MessageType.INVITE_RESPONSE),
        @JsonSubTypes.Type(value = AcceptInviteRequestRecord.class, name = MessageType.ACCEPT_INVITE_REQUEST),
        @JsonSubTypes.Type(value = AcceptInviteResponseRecord.class, name = MessageType.ACCEPT_INVITE_RESPONSE),
        @JsonSubTypes.Type(value = RejectInviteRequestRecord.class, name = MessageType.REJECT_INVITE_REQUEST),
        @JsonSubTypes.Type(value = RejectInviteResponseRecord.class, name = MessageType.REJECT_INVITE_RESPONSE),
        @JsonSubTypes.Type(value = DisconnectRequestRecord.class, name = MessageType.DISCONNECT_REQUEST),
        @JsonSubTypes.Type(value = DisconnectResponseRecord.class, name = MessageType.DISCONNECT_RESPONSE),

        @JsonSubTypes.Type(value = FetchChannelInviteCodeRequestRecord.class, name = MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST),
        @JsonSubTypes.Type(value = FetchChannelInviteCodeResponseRecord.class, name = MessageType.FETCH_CHANNEL_INVITE_CODE_RESPONSE),
        @JsonSubTypes.Type(value = FetchChannelsRequestRecord.class, name = MessageType.FETCH_CHANNELS_REQUEST),
        @JsonSubTypes.Type(value = FetchChannelsResponseRecord.class, name = MessageType.FETCH_CHANNELS_RESPONSE),
        @JsonSubTypes.Type(value = CreateChannelRequestRecord.class, name = MessageType.CREATE_CHANNEL_REQUEST),
        @JsonSubTypes.Type(value = CreateChannelResponseRecord.class, name = MessageType.CREATE_CHANNEL_RESPONSE),
        @JsonSubTypes.Type(value = EnterChannelRequestRecord.class, name = MessageType.ENTER_CHANNEL_REQUEST),
        @JsonSubTypes.Type(value = EnterChannelResponseRecord.class, name = MessageType.ENTER_CHANNEL_RESPONSE),
        @JsonSubTypes.Type(value = JoinChannelRequestRecord.class, name = MessageType.JOIN_CHANNEL_REQUEST),
        @JsonSubTypes.Type(value = JoinChannelResponseRecord.class, name = MessageType.JOIN_CHANNEL_RESPONSE),
        @JsonSubTypes.Type(value = LeaveChannelRequestRecord.class, name = MessageType.LEAVE_CHANNEL_REQUEST),
        @JsonSubTypes.Type(value = LeaveChannelResponseRecord.class, name = MessageType.LEAVE_CHANNEL_RESPONSE),
        @JsonSubTypes.Type(value = QuitChannelRequestRecord.class, name = MessageType.QUIT_CHANNEL_REQUEST),
        @JsonSubTypes.Type(value = QuitChannelResponseRecord.class, name = MessageType.QUIT_CHANNEL_RESPONSE),

        @JsonSubTypes.Type(value = WriteMessageRecord.class, name = MessageType.WRITE_MESSAGE),
        @JsonSubTypes.Type(value = FetchMessagesRequestRecord.class, name = MessageType.FETCH_MESSAGES_REQUEST),
        @JsonSubTypes.Type(value = FetchMessagesResponseRecord.class, name = MessageType.FETCH_MESSAGES_RESPONSE),
        @JsonSubTypes.Type(value = WriteMessageAckRecord.class, name = MessageType.WRITE_MESSAGE_ACK),
        @JsonSubTypes.Type(value = ReadMessageAckRecord.class, name = MessageType.READ_MESSAGE_ACK),
        @JsonSubTypes.Type(value = MessageNotificationRecord.class, name = MessageType.NOTIFY_MESSAGE),
        @JsonSubTypes.Type(value = InviteNotificationRecord.class, name = MessageType.ASK_INVITE),
        @JsonSubTypes.Type(value = AcceptInviteNotificationRecord.class, name = MessageType.NOTIFY_ACCEPT_INVITE),
        @JsonSubTypes.Type(value = JoinChannelNotificationRecord.class, name = MessageType.NOTIFY_JOIN_CHANNEL),
        @JsonSubTypes.Type(value = ErrorResponseRecord.class, name = MessageType.ERROR)
})
public interface RecordInterface {

    String type();
}
