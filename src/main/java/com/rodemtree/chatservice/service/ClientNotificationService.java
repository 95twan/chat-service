package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.*;
import com.rodemtree.chatservice.kafka.KafkaProducer;
import com.rodemtree.chatservice.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClientNotificationService {


    private static final Logger log = LoggerFactory.getLogger(ClientNotificationService.class);

    private final SessionService sessionService;
    private final KafkaProducer kafkaProducer;
    private final PushService pushService;
    private final JsonUtil jsonUtil;


    public ClientNotificationService(SessionService sessionService, KafkaProducer kafkaProducer, PushService pushService, JsonUtil jsonUtil) {
        this.sessionService = sessionService;
        this.kafkaProducer = kafkaProducer;
        this.pushService = pushService;
        this.jsonUtil = jsonUtil;

        pushService.registerPushMessageType(MessageType.INVITE_RESPONSE, InviteResponseRecord.class);
        pushService.registerPushMessageType(MessageType.ASK_INVITE, InviteNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.ACCEPT_INVITE_RESPONSE, AcceptInviteResponseRecord.class);
        pushService.registerPushMessageType(MessageType.NOTIFY_ACCEPT_INVITE, AcceptInviteNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.NOTIFY_JOIN_CHANNEL, JoinChannelNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.DISCONNECT_RESPONSE, DisconnectResponseRecord.class);
        pushService.registerPushMessageType(MessageType.REJECT_INVITE_RESPONSE, RejectInviteResponseRecord.class);
        pushService.registerPushMessageType(MessageType.CREATE_CHANNEL_RESPONSE, CreateChannelResponseRecord.class);
        pushService.registerPushMessageType(MessageType.QUIT_CHANNEL_RESPONSE, QuitChannelResponseRecord.class);
    }

    public void sendMessage(UserId userId, RecordInterface recordInterface) {
        sessionService.getListenTopic(userId).ifPresentOrElse(
                topic -> kafkaProducer.sendResponse(topic, recordInterface),
                () -> pushService.pushMessage(recordInterface)
        );
    }

    public void sendMessageUsingPartitionKey(ChannelId channelId, UserId userId, RecordInterface recordInterface) {
        sessionService.getListenTopic(userId).ifPresentOrElse(
                topic -> kafkaProducer.sendMessageUsingPartitionKey(topic, channelId, userId, recordInterface),
                () -> pushService.pushMessage(recordInterface)
        );
    }

    public void sendError(ErrorResponseRecord errorResponseRecord) {
        sessionService.getListenTopic(errorResponseRecord.userId()).ifPresentOrElse(
                topic -> kafkaProducer.sendResponse(topic, errorResponseRecord),
                () -> log.warn("Send error failed. Type: {}, Error: {}, User: {} is offline",
                        errorResponseRecord.type(),
                        errorResponseRecord.message(),
                        errorResponseRecord.userId())
        );
    }
}
