package com.rodemtree.chatservice.dto.kafka.outbound;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rodemtree.chatservice.constant.MessageType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InviteResponseRecord.class, name = MessageType.INVITE_RESPONSE),
        @JsonSubTypes.Type(value = AcceptInviteResponseRecord.class, name = MessageType.ACCEPT_INVITE_RESPONSE),
        @JsonSubTypes.Type(value = RejectInviteResponseRecord.class, name = MessageType.REJECT_INVITE_RESPONSE),
        @JsonSubTypes.Type(value = DisconnectResponseRecord.class, name = MessageType.DISCONNECT_RESPONSE),
        @JsonSubTypes.Type(value = CreateChannelResponseRecord.class, name = MessageType.CREATE_CHANNEL_RESPONSE),
        @JsonSubTypes.Type(value = QuitChannelResponseRecord.class, name = MessageType.QUIT_CHANNEL_RESPONSE),

        @JsonSubTypes.Type(value = MessageNotificationRecord.class, name = MessageType.NOTIFY_MESSAGE),
        @JsonSubTypes.Type(value = InviteNotificationRecord.class, name = MessageType.ASK_INVITE),
        @JsonSubTypes.Type(value = AcceptInviteNotificationRecord.class, name = MessageType.NOTIFY_ACCEPT_INVITE),
        @JsonSubTypes.Type(value = JoinChannelNotificationRecord.class, name = MessageType.NOTIFY_JOIN_CHANNEL),
})
public interface RecordInterface {

    String type();
}
