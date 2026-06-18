package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.outbound.BaseMessage;
import com.rodemtree.chatservice.entity.MessageEntity;
import com.rodemtree.chatservice.repository.MessageRepository;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import com.rodemtree.chatservice.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private static final int SENDER_THREAD_POOL_SIZE = 10;

    private final ChannelService channelService;
    private final PushService pushService;
    private final WebSocketSessionManager webSocketSessionManager;
    private final JsonUtil jsonUtil;
    private final MessageRepository messageRepository;
    private final ExecutorService senderThreadPool = Executors.newFixedThreadPool(SENDER_THREAD_POOL_SIZE);

    public MessageService(ChannelService channelService, PushService pushService, WebSocketSessionManager webSocketSessionManager, JsonUtil jsonUtil, MessageRepository messageRepository) {
        this.channelService = channelService;
        this.pushService = pushService;
        this.webSocketSessionManager = webSocketSessionManager;
        this.jsonUtil = jsonUtil;
        this.messageRepository = messageRepository;

        pushService.registerPushMessageType(MessageType.NOTIFY_MESSAGE);
    }

    @Transactional
    public void sendMessage(UserId senderUserId, ChannelId channelId, String content, BaseMessage message) {
        Optional<String> json = jsonUtil.toJson(message);
        if (json.isEmpty()) {
            log.error("Send message failed. messageType: {}", message.getType());
            return;
        }
        String payload = json.get();

        try {
            messageRepository.save(new MessageEntity(senderUserId.id(), content));
        } catch (Exception ex) {
            log.error("Send message failed. cause: {}", ex.getMessage());
            return;
        }

        List<UserId> allParticipants = channelService.getParticipantIds(channelId);
        List<UserId> onlineParticipants = channelService.getOnlineParticipantIds(channelId, allParticipants);

        for (int i = 0; i < allParticipants.size(); i++) {
            UserId participantId = allParticipants.get(i);
            if (senderUserId.equals(participantId)) {
                continue;
            }
            if (onlineParticipants.get(i) != null) {
                CompletableFuture.runAsync(() -> {
                    try {
                        WebSocketSession session = webSocketSessionManager.getSession(participantId);
                        if (session != null) {
                            webSocketSessionManager.sendMessage(session, payload);
                        } else {
                            pushService.pushMessage(participantId, MessageType.NOTIFY_MESSAGE, payload);
                        }
                    } catch (Exception ex) {
                        pushService.pushMessage(participantId, MessageType.NOTIFY_MESSAGE, payload);
                    }
                }, senderThreadPool);
            } else {
                pushService.pushMessage(participantId, MessageType.NOTIFY_MESSAGE, payload);
            }
        }

        // 반복문을 사용한 메시지 직렬 전송
//        channelService.getOnlineParticipantIds(channelId).stream()
//                .filter(participantId -> !participantId.equals(senderUserId))
//                .forEach(messageSender::accept);


        // 쓰레드를 사용한 메시지 병렬 전송
//        channelService.getOnlineParticipantIds(channelId).stream()
//                .filter(participantId -> !participantId.equals(senderUserId))
//                .forEach(participantId -> {
//                    CompletableFuture.runAsync(() -> {
//                        messageSender.accept(participantId);
//                    }, senderThreadPool);
//                });
    }
}
