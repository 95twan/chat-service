package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.KeyPrefix;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository<? extends Session> httpSessionRepository;
    private final CacheService cacheService;
    private final long TTL = 300;


    public List<UserId> getOnlineParticipants(ChannelId channelId, List<UserId> userIds) {
        List<String> channelIdKeys = userIds.stream().map(this::buildChannelIdKey).toList();
        List<String> channelIds = cacheService.get(channelIdKeys);
        if (channelIds != null) {
            List<UserId> onlineParticipantUserIds = new ArrayList<>(channelIds.size());
            String chId = channelId.id().toString();
            for (int i = 0; i < userIds.size(); i++) {
                String value = channelIds.get(i);
                onlineParticipantUserIds.add(value != null && value.equals(chId) ? userIds.get(i) : null);
            }
            return onlineParticipantUserIds;
        }

        return Collections.emptyList();
    }

    public boolean setActiveChannel(UserId userId, ChannelId channelId) {
        String channelIdKey = buildChannelIdKey(userId);
        return cacheService.set(channelIdKey, channelId.id().toString(), TTL);
    }

    public boolean removeActiveChannel(UserId userId) {
        String channelIdKey = buildChannelIdKey(userId);
        return cacheService.delete(channelIdKey);
    }

    public void refreshTTL(UserId userId, String httpSessionId) {
        String channelIdKey = buildChannelIdKey(userId);
        try {
            Session httpSession = httpSessionRepository.findById(httpSessionId);
            if (httpSession != null) {
                httpSession.setLastAccessedTime(Instant.now());
                cacheService.expire(channelIdKey, TTL);
            }
        } catch (Exception ex) {
            log.error("Find failed. httpSessionId: {}, cause: {}", httpSessionId, ex.getMessage());
        }
    }

    private String buildChannelIdKey(UserId userId) {
        return cacheService.buildKey(KeyPrefix.USER, userId.id().toString(), IdKey.CHANNEL_ID.getValue());
    }
}
