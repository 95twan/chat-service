package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.projection.ChannelTitleProjection;
import com.rodemtree.chatservice.dto.projection.InviteCodeProjection;
import com.rodemtree.chatservice.entity.ChannelEntity;
import com.rodemtree.chatservice.entity.UserChannelEntity;
import com.rodemtree.chatservice.repository.ChannelRepository;
import com.rodemtree.chatservice.repository.UserChannelRepository;
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

    public Optional<InviteCode> getChannelInviteCode(ChannelId channelId) {
        Optional<InviteCode> inviteCode = channelRepository.findChannelInviteCodeByChannelId(channelId.id())
                .map(projection -> new InviteCode(projection.getInviteCode()));

        if (inviteCode.isEmpty()) {
            log.warn("Invite code is not exist. channelId: {}", channelId);
        }
        return inviteCode;
    }

    public boolean isJoined(UserId userId, ChannelId channelId) {
        return userChannelRepository.existsByUserIdAndChannelId(userId.id(), channelId.id());
    }

    public List<UserId> getParticipantIds(ChannelId channelId) {
        return userChannelRepository.findUserIdsByChannelId(channelId.id()).stream()
                .map(userId -> new UserId(userId.getUserId()))
                .toList();
    }

    public List<UserId> getOnlineParticipantIds(ChannelId channelId) {
        return sessionService.getOnlineParticipants(channelId, getParticipantIds(channelId));
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
}
