package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.database.ShardContext;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.projection.MessageInfoProjection;
import com.rodemtree.chatservice.entity.MessageEntity;
import com.rodemtree.chatservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageShardService {

    private final MessageRepository messageRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public MessageSeqId findLastMessageSequenceByChannelId(ChannelId channelId) {
        try (ShardContext.ShardContextScope ignored = new ShardContext.ShardContextScope(channelId.id())) {
            return messageRepository.findLastMessageSequenceByChannelId(channelId.id())
                    .map(MessageSeqId::new).orElse(new MessageSeqId(0L));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<MessageInfoProjection> findMessageInfoByChannelIdAndMessageSequenceBetween(ChannelId channelId, MessageSeqId startMessageSequence, MessageSeqId endMessageSequence) {
        try (ShardContext.ShardContextScope ignored = new ShardContext.ShardContextScope(channelId.id())) {
            return messageRepository.findMessageInfoByChannelIdAndMessageSequenceBetween(channelId.id(), startMessageSequence.id(), endMessageSequence.id());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(ChannelId channelId, MessageSeqId messageSeqId, UserId senderUserId, String content) {
        try (ShardContext.ShardContextScope ignored = new ShardContext.ShardContextScope(channelId.id())) {
            messageRepository.save(new MessageEntity(channelId.id(), messageSeqId.id(), senderUserId.id(), content));
        }
    }
}
