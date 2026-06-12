package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.entity.MessageEntity;
import com.rodemtree.chatservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private static final int SENDER_THREAD_POOL_SIZE = 10;

    private final MessageRepository messageRepository;
    private final ChannelService channelService;
    private final ExecutorService senderThreadPool = Executors.newFixedThreadPool(SENDER_THREAD_POOL_SIZE);


    public void sendMessage(UserId senderUserId, ChannelId channelId, String content, Consumer<UserId> messageSender) {
        try {
            messageRepository.save(new MessageEntity(senderUserId.id(), content));
        } catch (Exception ex) {
            log.error("Send message failed. cause: {}", ex.getMessage());
            return;
        }

        // 반복문을 사용한 메시지 직렬 전송
//        channelService.getOnlineParticipantIds(channelId).stream()
//                .filter(participantId -> !participantId.equals(senderUserId))
//                .forEach(messageSender::accept);


        // 쓰레드를 사용한 메시지 병렬 전송
        channelService.getOnlineParticipantIds(channelId).stream()
                .filter(participantId -> !participantId.equals(senderUserId))
                .forEach(participantId -> {
                    CompletableFuture.runAsync(() -> {
                        messageSender.accept(participantId);
                    }, senderThreadPool);
                });
    }
}
