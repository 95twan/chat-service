package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.KeyPrefix;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.*;
import com.rodemtree.chatservice.dto.projection.ChannelTitleProjection;
import com.rodemtree.chatservice.entity.ChannelEntity;
import com.rodemtree.chatservice.entity.UserChannelEntity;
import com.rodemtree.chatservice.repository.ChannelRepository;
import com.rodemtree.chatservice.repository.MessageRepository;
import com.rodemtree.chatservice.repository.UserChannelRepository;
import com.rodemtree.chatservice.util.JsonUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private static final Logger log = LoggerFactory.getLogger(ChannelService.class);
    private static final int LIMIT_HEAD_COUNT = 100;

    private final SessionService sessionService;
    private final UserConnectionService userConnectionService;
    private final CacheService cacheService;
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserChannelRepository userChannelRepository;
    private final JsonUtil jsonUtil;
    private final long TTL = 600;

    @Transactional(readOnly = true)
    public Optional<InviteCode> getChannelInviteCode(ChannelId channelId) {
        String key = cacheService.buildKey(KeyPrefix.CHANNEL_INVITE_CODE, channelId.id().toString());
        Optional<String> cachedInviteCode = cacheService.get(key);

        if (cachedInviteCode.isPresent()) {
            return Optional.of(new InviteCode(cachedInviteCode.get()));
        }

        Optional<InviteCode> fromDB = channelRepository.findChannelInviteCodeByChannelId(channelId.id())
                .map(projection -> new InviteCode(projection.getInviteCode()));

        if (fromDB.isEmpty()) {
            log.warn("Invite code is not exist. channelId: {}", channelId);
        }
        fromDB.ifPresent(inviteCode -> cacheService.set(key, inviteCode.code(), TTL));
        return fromDB;
    }

    @Transactional(readOnly = true)
    public boolean isJoined(UserId userId, ChannelId channelId) {
        String key = cacheService.buildKey(KeyPrefix.JOINED_CHANNEL, userId.id().toString(), channelId.id().toString());
        Optional<String> cachedChannel = cacheService.get(key);
        if (cachedChannel.isPresent()) {
            return true;
        }
        boolean fromDB = userChannelRepository.existsByUserIdAndChannelId(userId.id(), channelId.id());
        if (fromDB) {
            cacheService.set(key, "T", TTL);
        }
        return fromDB;
    }

    @Transactional(readOnly = true)
    public List<UserId> getParticipantIds(ChannelId channelId) {
        String key = cacheService.buildKey(KeyPrefix.PARTICIPANT_IDS, channelId.id().toString());
        Optional<String> cachedParticipantIds = cacheService.get(key);
        if (cachedParticipantIds.isPresent()) {
            return jsonUtil.fromJsonToList(cachedParticipantIds.get(), String.class).stream()
                    .map(userId -> new UserId(Long.valueOf(userId)))
                    .toList();
        }
        List<UserId> fromDB = userChannelRepository.findUserIdsByChannelId(channelId.id()).stream()
                .map(userId -> new UserId(userId.getUserId()))
                .toList();
        if (!fromDB.isEmpty()) {
            jsonUtil.toJson(fromDB.stream().map(UserId::id).toList()).ifPresent(json -> cacheService.set(key, json, TTL));
        }
        return fromDB;
    }

    public List<UserId> getOnlineParticipantIds(ChannelId channelId, List<UserId> userIds) {
        return sessionService.getOnlineParticipants(channelId, userIds);
    }

    @Transactional(readOnly = true)
    public Optional<Channel> getChannel(InviteCode inviteCode) {
        String key = cacheService.buildKey(KeyPrefix.CHANNEL, inviteCode.code());
        Optional<String> cachedChannel = cacheService.get(key);
        if (cachedChannel.isPresent()) {
            return jsonUtil.fromJson(cachedChannel.get(), Channel.class);
        }
        Optional<Channel> fromDB = channelRepository.findChannelByInviteCode(inviteCode.code())
                .map(projection -> new Channel(new ChannelId(projection.getChannelId()), projection.getTitle(), projection.getHeadCount()));
        fromDB.flatMap(jsonUtil::toJson).ifPresent(json -> cacheService.set(key, json, TTL));
        return fromDB;
    }

    @Transactional(readOnly = true)
    public List<Channel> getChannels(UserId userId) {
        String key = cacheService.buildKey(KeyPrefix.CHANNELS, userId.id().toString());
        Optional<String> cachedChannels = cacheService.get(key);
        if (cachedChannels.isPresent()) {
            return jsonUtil.fromJsonToList(cachedChannels.get(), Channel.class);
        }
        List<Channel> fromDB = userChannelRepository.findChannelsByUserId(userId.id()).stream()
                .map(projection ->
                        new Channel(new ChannelId(projection.getChannelId()), projection.getTitle(), projection.getHeadCount())
                )
                .toList();
        if (!fromDB.isEmpty()) {
            jsonUtil.toJson(fromDB).ifPresent(json -> cacheService.set(key, json, TTL));
        }
        return fromDB;
    }

    @Transactional
    public Pair<Optional<Channel>, ResultType> createChannel(UserId creatorUserId, List<UserId> participantIds, String title) {
        if (title == null || title.isEmpty()) {
            log.warn("Invalid args : title is empty.");
            return Pair.of(Optional.empty(), ResultType.INVALID_ARGS);
        }

        // me + participants
        int headCount = 1 + participantIds.size();
        if (headCount > LIMIT_HEAD_COUNT) {
            log.warn("Over limit of channel. creatorUserId: {}, participantIds count={}, title={}", creatorUserId, participantIds.size(), title);
            return Pair.of(Optional.empty(), ResultType.OVER_LIMIT);
        }

        if (userConnectionService.countConnectionStatus(creatorUserId, participantIds, UserConnectionStatus.ACCEPTED) != participantIds.size()) {
            log.warn("Included unconnected user. participantIds : {}", participantIds);
            return Pair.of(Optional.empty(), ResultType.NOT_ALLOWED);
        }

        try {
            ChannelEntity channelEntity = channelRepository.save(new ChannelEntity(title, headCount));
            Long channelId = channelEntity.getChannelId();
            List<UserChannelEntity> userChannelEntities = participantIds.stream()
                    .map(participantId -> new UserChannelEntity(participantId.id(), channelId, 0L))
                    .collect(Collectors.toList());
            userChannelEntities.add(new UserChannelEntity(creatorUserId.id(), channelId, 0L));
            userChannelRepository.saveAll(userChannelEntities);
            cacheService.delete(cacheService.buildKey(KeyPrefix.CHANNELS, creatorUserId.id().toString()));
            participantIds.forEach(participantId -> cacheService.delete(cacheService.buildKey(KeyPrefix.CHANNELS, participantId.id().toString())));
            Channel channel = new Channel(new ChannelId(channelId), title, headCount);
            return Pair.of(Optional.of(channel), ResultType.SUCCESS);
        } catch (Exception ex) {
            log.error("Failed to create channel. cause: {}", ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public Pair<Optional<Channel>, ResultType> joinChannel(InviteCode inviteCode, UserId userId) {
        Optional<Channel> ch = getChannel(inviteCode);
        if (ch.isEmpty()) {
            return Pair.of(Optional.empty(), ResultType.NOT_FOUND);
        }

        Channel channel = ch.get();

        if (isJoined(userId, channel.channelId())) {
            return Pair.of(Optional.empty(), ResultType.ALREADY_JOINED);
        } else if (channel.headCount() >= LIMIT_HEAD_COUNT) {
            return Pair.of(Optional.empty(), ResultType.OVER_LIMIT);
        }

        ChannelEntity channelEntity = channelRepository.findChannelForUpdateByChannelId(channel.channelId().id())
                .orElseThrow(() -> new EntityNotFoundException("Invalid channelId: " + channel.channelId().id()));

        if (channelEntity.getHeadCount() < LIMIT_HEAD_COUNT) {
            channelEntity.setHeadCount(channelEntity.getHeadCount() + 1);
            userChannelRepository.save(new UserChannelEntity(userId.id(), channelEntity.getChannelId(), 0L));
            cacheService.delete(
                    List.of(
                            cacheService.buildKey(KeyPrefix.CHANNELS, userId.id().toString()),
                            cacheService.buildKey(KeyPrefix.CHANNEL, channelEntity.getInviteCode()),
                            cacheService.buildKey(KeyPrefix.PARTICIPANT_IDS, channelEntity.getChannelId().toString())
                    )
            );
        }

        return Pair.of(Optional.of(channel), ResultType.SUCCESS);
    }

    @Transactional(readOnly = true)
    public Pair<Optional<ChannelEntry>, ResultType> enterChannel(UserId userId, ChannelId channelId) {
        if (!isJoined(userId, channelId)) {
            log.warn("Enter channel failed. User not joined the channel. userId: {}, channelId: {}", userId.id(), channelId.id());
            return Pair.of(Optional.empty(), ResultType.NOT_JOINED);
        }

        Optional<String> title = channelRepository.findChannelTitleByChannelId(channelId.id()).map(ChannelTitleProjection::getTitle);
        if (title.isEmpty()) {
            log.warn("Enter channel failed. Channel does not exist. userId: {}, channelId: {}", userId.id(), channelId.id());
            return Pair.of(Optional.empty(), ResultType.NOT_FOUND);
        }

        Optional<MessageSeqId> lastReadMessageSeq = userChannelRepository.findLastReadMessageSeqByUserIdAndChannelId(userId.id(), channelId.id())
                .map(projection -> new MessageSeqId(projection.getLastReadMessageSeq()));
        if (lastReadMessageSeq.isEmpty()) {
            log.error("Enter channel failed. No record found for UserId: {} and ChannelId: {}", userId.id(), channelId.id());
            return Pair.of(Optional.empty(), ResultType.NOT_FOUND);
        }

        MessageSeqId lastMessageSeqId = messageRepository.findLastMessageSequenceByChannelId(channelId.id())
                .map(MessageSeqId::new).orElse(new MessageSeqId(0L));

        if (sessionService.setActiveChannel(userId, channelId)) {
            return Pair.of(Optional.of(new ChannelEntry(title.get(), lastReadMessageSeq.get(), lastMessageSeqId)), ResultType.SUCCESS);
        }

        log.error("Enter channel failed. userId: {}, channelId: {}", userId.id(), channelId.id());
        return Pair.of(Optional.empty(), ResultType.FAILED);
    }

    public boolean leaveChannel(UserId userId) {
        return sessionService.removeActiveChannel(userId);
    }

    @Transactional
    public ResultType quitChannel(UserId userId, ChannelId channelId) {
        if (!isJoined(userId, channelId)) {
            return ResultType.NOT_JOINED;
        }

        ChannelEntity channelEntity = channelRepository.findChannelForUpdateByChannelId(channelId.id())
                .orElseThrow(() -> new EntityNotFoundException("Invalid channelId " + channelId.id()));

        if (channelEntity.getHeadCount() > 0) {
            channelEntity.setHeadCount(channelEntity.getHeadCount() - 1);
        } else {
            log.error("Count is already zero. channelId: {}, userId: {}", channelId.id(), userId.id());
        }

        userChannelRepository.deleteByUserIdAndChannelId(userId.id(), channelId.id());
        cacheService.delete(
                List.of(
                        cacheService.buildKey(KeyPrefix.CHANNELS, userId.id().toString()),
                        cacheService.buildKey(KeyPrefix.CHANNEL, channelEntity.getInviteCode()),
                        cacheService.buildKey(KeyPrefix.PARTICIPANT_IDS, channelEntity.getChannelId().toString()),
                        cacheService.buildKey(KeyPrefix.JOINED_CHANNEL, userId.id().toString(), channelId.id().toString())
                )
        );
        return ResultType.SUCCESS;
    }
}
