package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.Message;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.outbound.MessageNotificationRecord;
import com.rodemtree.chatservice.dto.projection.MessageInfoProjection;
import com.rodemtree.chatservice.dto.websocket.outbound.BaseMessage;
import com.rodemtree.chatservice.dto.websocket.outbound.WriteMessageAck;
import com.rodemtree.chatservice.repository.UserChannelRepository;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import com.rodemtree.chatservice.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private static final int SENDER_THREAD_POOL_SIZE = 10;

    private final UserService userService;
    private final ChannelService channelService;
    private final PushService pushService;
    private final WebSocketSessionManager webSocketSessionManager;
    private final MessageShardService messageShardService;
    private final JsonUtil jsonUtil;
    private final UserChannelRepository userChannelRepository;
    private final ExecutorService senderThreadPool = Executors.newFixedThreadPool(SENDER_THREAD_POOL_SIZE);

    public MessageService(
            UserService userService,
            ChannelService channelService,
            PushService pushService,
            WebSocketSessionManager webSocketSessionManager,
            MessageShardService messageShardService,
            JsonUtil jsonUtil,
            UserChannelRepository userChannelRepository
    ) {
        this.userService = userService;
        this.channelService = channelService;
        this.pushService = pushService;
        this.webSocketSessionManager = webSocketSessionManager;
        this.messageShardService = messageShardService;
        this.jsonUtil = jsonUtil;
        this.userChannelRepository = userChannelRepository;

        pushService.registerPushMessageType(MessageType.NOTIFY_MESSAGE, MessageNotificationRecord.class);
    }

    @Transactional(readOnly = true)
    public Pair<List<Message>, ResultType> getMessages(ChannelId channelId, MessageSeqId startMessageSeqId, MessageSeqId endMessageSeqId) {
        List<MessageInfoProjection> messageInfos = messageShardService.findMessageInfoByChannelIdAndMessageSequenceBetween(channelId, startMessageSeqId, endMessageSeqId);
        Set<UserId> userIds = messageInfos.stream()
                .map(projection -> new UserId(projection.getSenderUserId()))
                .collect(Collectors.toUnmodifiableSet());

        if (userIds.isEmpty()) {
            return Pair.of(Collections.emptyList(), ResultType.SUCCESS);
        }

        Pair<Map<UserId, String>, ResultType> result = userService.getUsernames(userIds);

        if (result.getSecond() == ResultType.SUCCESS) {
            List<Message> messages = messageInfos.stream()
                    .map(projection -> {
                        UserId userId = new UserId(projection.getSenderUserId());
                        return new Message(
                                channelId,
                                new MessageSeqId(projection.getMessageSequence()),
                                result.getFirst().getOrDefault(userId, "unknown"),
                                projection.getContent()
                        );
                    })
                    .toList();
            return Pair.of(messages, ResultType.SUCCESS);
        } else {
            return Pair.of(Collections.emptyList(), result.getSecond());
        }
    }

    @Transactional
    public void sendMessage(
            UserId senderUserId,
            ChannelId channelId,
            MessageSeqId messageSeqId,
            Long serial,
            String content,
            BaseMessage message
    ) {
        Optional<String> json = jsonUtil.toJson(message);
        if (json.isEmpty()) {
            log.error("Send message failed. messageType: {}", message.getType());
            return;
        }
        String payload = json.get();

        try {
            messageShardService.save(channelId, messageSeqId, senderUserId, content);
        } catch (Exception ex) {
            log.error("Send message failed. cause: {}", ex.getMessage());
            return;
        }

        List<UserId> allParticipants = channelService.getParticipantIds(channelId);
        List<UserId> onlineParticipants = channelService.getOnlineParticipantIds(channelId, allParticipants);

        for (int i = 0; i < allParticipants.size(); i++) {
            UserId participantId = allParticipants.get(i);
            if (senderUserId.equals(participantId)) {
                updateLastReadMessageSeq(senderUserId, channelId, messageSeqId);
                jsonUtil.toJson(new WriteMessageAck(serial, messageSeqId)).ifPresent(writeMessageAck ->
                        CompletableFuture.runAsync(() -> {
                            try {
                                WebSocketSession session = webSocketSessionManager.getSession(participantId);
                                if (session != null) {
                                    webSocketSessionManager.sendMessage(session, writeMessageAck);
                                }
                            } catch (Exception ex) {
                                log.error("Send writeMessageAck failed. userId: {}, cause: {}", participantId.id(), ex.getMessage());
                            }
                        }, senderThreadPool)
                );
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

    @Transactional
    public void updateLastReadMessageSeq(UserId userId, ChannelId channelId, MessageSeqId messageSeqId) {
        if (userChannelRepository.updateLastReadMessageSeqByUserIdAndChannelId(userId.id(), channelId.id(), messageSeqId.id()) == 0) {
            log.error("Update lastReadMessageSeq failed. No record found for UserId: {} and ChannelId: {}", userId.id(), channelId.id());
        }
    }
}
