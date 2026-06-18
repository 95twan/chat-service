package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.projection.ChannelTitleProjection;
import com.rodemtree.chatservice.entity.ChannelEntity;
import com.rodemtree.chatservice.entity.UserChannelEntity;
import com.rodemtree.chatservice.repository.ChannelRepository;
import com.rodemtree.chatservice.repository.UserChannelRepository;
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
    private final ChannelRepository channelRepository;
    private final UserChannelRepository userChannelRepository;

    @Transactional(readOnly = true)
    public Optional<InviteCode> getChannelInviteCode(ChannelId channelId) {
        Optional<InviteCode> inviteCode = channelRepository.findChannelInviteCodeByChannelId(channelId.id())
                .map(projection -> new InviteCode(projection.getInviteCode()));

        if (inviteCode.isEmpty()) {
            log.warn("Invite code is not exist. channelId: {}", channelId);
        }
        return inviteCode;
    }

    @Transactional(readOnly = true)
    public boolean isJoined(UserId userId, ChannelId channelId) {
        return userChannelRepository.existsByUserIdAndChannelId(userId.id(), channelId.id());
    }

    @Transactional(readOnly = true)
    public List<UserId> getParticipantIds(ChannelId channelId) {
        return userChannelRepository.findUserIdsByChannelId(channelId.id()).stream()
                .map(userId -> new UserId(userId.getUserId()))
                .toList();
    }

    public List<UserId> getOnlineParticipantIds(ChannelId channelId, List<UserId> userIds) {
        return sessionService.getOnlineParticipants(channelId, userIds);
    }

    @Transactional(readOnly = true)
    public Optional<Channel> getChannel(InviteCode inviteCode) {
        return channelRepository.findChannelByInviteCode(inviteCode.code())
                .map(projection -> new Channel(new ChannelId(projection.getChannelId()), projection.getTitle(), projection.getHeadCount()));
    }

    @Transactional(readOnly = true)
    public List<Channel> getChannels(UserId userId) {
        return userChannelRepository.findChannelsByUserId(userId.id()).stream()
                .map(projection ->
                        new Channel(new ChannelId(projection.getChannelId()), projection.getTitle(), projection.getHeadCount())
                )
                .toList();
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
        }

        return Pair.of(Optional.of(channel), ResultType.SUCCESS);
    }

    @Transactional(readOnly = true)
    public Pair<Optional<String>, ResultType> enterChannel(UserId userId, ChannelId channelId) {
        if (!isJoined(userId, channelId)) {
            log.warn("Enter channel failed. User not joined the channel. userId: {}, channelId: {}", userId.id(), channelId.id());
            return Pair.of(Optional.empty(), ResultType.NOT_JOINED);
        }

        Optional<String> title = channelRepository.findChannelTitleByChannelId(channelId.id()).map(ChannelTitleProjection::getTitle);
        if (title.isEmpty()) {
            log.warn("Enter channel failed. Channel does not exist. userId: {}, channelId: {}", userId.id(), channelId.id());
            return Pair.of(Optional.empty(), ResultType.NOT_FOUND);
        }

        if (sessionService.setActiveChannel(userId, channelId)) {
            return Pair.of(title, ResultType.SUCCESS);
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
        return ResultType.SUCCESS;
    }
}
