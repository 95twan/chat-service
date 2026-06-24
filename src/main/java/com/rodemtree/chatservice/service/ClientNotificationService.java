package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.outbound.*;
import com.rodemtree.chatservice.dto.websocket.outbound.BaseMessage;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import com.rodemtree.chatservice.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Service
public class ClientNotificationService {


    private static final Logger log = LoggerFactory.getLogger(ClientNotificationService.class);

    private final WebSocketSessionManager webSocketSessionManager;
    private final PushService pushService;
    private final JsonUtil jsonUtil;


    public ClientNotificationService(WebSocketSessionManager webSocketSessionManager, PushService pushService, JsonUtil jsonUtil) {
        this.webSocketSessionManager = webSocketSessionManager;
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

    public void sendMessage(WebSocketSession session, UserId userId, BaseMessage message) {
        sendPayload(session, userId, message);
    }

    public void sendMessage(UserId userId, BaseMessage message) {
        sendPayload(webSocketSessionManager.getSession(userId), userId, message);
    }

    private void sendPayload(WebSocketSession session, UserId userId, BaseMessage message) {
        Optional<String> json = jsonUtil.toJson(message);
        if (json.isEmpty()) {
            log.error("Send message failed. MessageType: {}", message.getType());
            return;
        }

        String payload = json.get();
        try {
            if (session != null) {
                webSocketSessionManager.sendMessage(session, payload);
            } else {
                pushService.pushMessage(userId, message.getType(), payload);
            }
        } catch (Exception ex) {
            pushService.pushMessage(userId, message.getType(), payload);
        }
    }
}
