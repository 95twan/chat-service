package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.projection.ChannelTitleProjection;
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

@Service
@RequiredArgsConstructor
public class ChannelService {

    private static final Logger log = LoggerFactory.getLogger(ChannelService.class);

    private final SessionService sessionService;
    private final UserConnectionService userConnectionService;
    private final ChannelRepository channelRepository;
    private final UserChannelRepository userChannelRepository;

    public boolean isJoined(UserId userId, ChannelId channelId) {
        return userChannelRepository.existsByUserIdAndChannelId(userId.id(), channelId.id());
    }

    public List<UserId> getParticipantIds(ChannelId channelId) {
        return userChannelRepository.findUserIdsByChannelId(channelId.id()).stream()
                .map(userId -> new UserId(userId.getUserId()))
                .toList();
    }

    public boolean isOnline(UserId userId, ChannelId channelId) {
        return sessionService.isOnline(userId, channelId);
    }

    @Transactional
    public Pair<Optional<Channel>, ResultType> createChannel(UserId creatorUserId, UserId participantId, String title) {
        if (title == null || title.isEmpty()) {
            log.warn("Invalid args : title is empty.");
            return Pair.of(Optional.empty(), ResultType.INVALID_ARGS);
        }

        if (userConnectionService.getStatus(creatorUserId, participantId) != UserConnectionStatus.ACCEPTED) {
            log.warn("Included unconnected user. participantId : {}", participantId);
            return Pair.of(Optional.empty(), ResultType.NOT_ALLOWED);
        }

        try {
            final int HEAD_COUNT = 2;
            ChannelEntity channelEntity = channelRepository.save(new ChannelEntity(title, HEAD_COUNT));
            Long channelId = channelEntity.getChannelId();
            List<UserChannelEntity> userChannelEntities = List.of(
                    new UserChannelEntity(creatorUserId.id(), channelId, 0L),
                    new UserChannelEntity(participantId.id(), channelId, 0L)
            );
            userChannelRepository.saveAll(userChannelEntities);
            Channel channel = new Channel(new ChannelId(channelId), title, HEAD_COUNT);
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
