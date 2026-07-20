package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.Message;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.MessageNotificationRecord;
import com.rodemtree.chatservice.dto.kafka.WriteMessageAckRecord;
import com.rodemtree.chatservice.dto.kafka.WriteMessageRecord;
import com.rodemtree.chatservice.dto.projection.MessageInfoProjection;
import com.rodemtree.chatservice.kafka.KafkaProducer;
import com.rodemtree.chatservice.repository.UserChannelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final UserService userService;
    private final ChannelService channelService;
    private final PushService pushService;
    private final SessionService sessionService;
    private final KafkaProducer kafkaProducer;
    private final MessageShardService messageShardService;
    private final UserChannelRepository userChannelRepository;

    public MessageService(
            UserService userService,
            ChannelService channelService,
            PushService pushService,
            SessionService sessionService,
            KafkaProducer kafkaProducer,
            MessageShardService messageShardService,
            UserChannelRepository userChannelRepository
    ) {
        this.userService = userService;
        this.channelService = channelService;
        this.pushService = pushService;
        this.sessionService = sessionService;
        this.kafkaProducer = kafkaProducer;
        this.messageShardService = messageShardService;
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
    public void sendMessage(WriteMessageRecord record) {
        ChannelId channelId = record.channelId();
        UserId senderUserId = record.userId();
        MessageSeqId messageSeqId = record.messageSeqId();
        String senderUsername = userService.getUsername(senderUserId).orElse("unknown");
        Long serial = record.serial();
        String content = record.content();

        try {
            messageShardService.save(channelId, messageSeqId, senderUserId, content);
        } catch (Exception ex) {
            log.error("Send message failed. cause: {}", ex.getMessage());
            return;
        }

        List<UserId> allParticipants = channelService.getParticipantIds(channelId);
        List<UserId> onlineParticipants = channelService.getOnlineParticipantIds(channelId, allParticipants);

        Map<String, List<UserId>> listenTopics = sessionService.getListenTopics(onlineParticipants);
        allParticipants.removeAll(onlineParticipants);

        listenTopics.forEach((listenTopic, participantIds) -> {
            if (participantIds.contains(senderUserId)) {
                updateLastReadMessageSeq(senderUserId, channelId, messageSeqId);
                kafkaProducer.sendMessageUsingPartitionKey(listenTopic, channelId, senderUserId, new WriteMessageAckRecord(senderUserId, serial, messageSeqId));
                participantIds.remove(senderUserId);
            } else {
                kafkaProducer.sendMessageUsingPartitionKey(
                        listenTopic,
                        channelId,
                        senderUserId,
                        new MessageNotificationRecord(senderUserId, channelId, messageSeqId, senderUsername, content, participantIds)
                );
            }
        });

        if (!allParticipants.isEmpty()) {
            pushService.pushMessage(new MessageNotificationRecord(senderUserId, channelId, messageSeqId, senderUsername, content, allParticipants));
        }

    }

    @Transactional
    public void updateLastReadMessageSeq(UserId userId, ChannelId channelId, MessageSeqId messageSeqId) {
        if (userChannelRepository.updateLastReadMessageSeqByUserIdAndChannelId(userId.id(), channelId.id(), messageSeqId.id()) == 0) {
            log.error("Update lastReadMessageSeq failed. No record found for UserId: {} and ChannelId: {}", userId.id(), channelId.id());
        }
    }
}
