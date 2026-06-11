package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.outbound.MessageNotification;
import com.rodemtree.chatservice.entity.MessageEntity;
import com.rodemtree.chatservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final ChannelService channelService;


    public void sendMessage(UserId senderUserId, ChannelId channelId, String content, Consumer<UserId> messageSender) {
        try {
            messageRepository.save(new MessageEntity(senderUserId.id(), content));
        } catch (Exception ex) {
            log.error("Send message failed. cause: {}", ex.getMessage());
            return;
        }

        List<UserId> participantIds = channelService.getParticipantIds(channelId);
        participantIds.stream()
                .filter(userId -> !userId.equals(senderUserId))
                .forEach(participantId -> {
                    if (channelService.isOnline(participantId, channelId)) {
                        messageSender.accept(participantId);
                    }
                });
    }
}
