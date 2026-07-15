package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.KeyPrefix;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final CacheService cacheService;
    private final long TTL = 300;

    public Optional<String> getListenTopic(UserId userId) {
        String key = buildUserSessionLocationKey(userId);
        return cacheService.get(key);
    }

    public Map<String, List<UserId>> getListenTopics(Collection<UserId> userIds) {
        List<String> keys = userIds.stream().map(this::buildUserSessionLocationKey).toList();
        List<String> listenToics = cacheService.get(keys);
        Map<String, List<UserId>> locationToUsers = new HashMap<>();
        Iterator<String> iterator = listenToics.iterator();

        for (UserId userId : userIds) {
            String listenTopic = iterator.next();
            if (listenTopic != null) {
                locationToUsers.computeIfAbsent(listenTopic, unused -> new ArrayList<>()).add(userId);
            }
        }
        return locationToUsers;
    }

    public List<UserId> getOnlineParticipants(ChannelId channelId, List<UserId> userIds) {
        List<String> channelIdKeys = userIds.stream().map(this::buildChannelIdKey).toList();
        List<String> channelIds = cacheService.get(channelIdKeys);
        if (channelIds != null) {
            List<UserId> onlineParticipantUserIds = new ArrayList<>(channelIds.size());
            String chId = channelId.id().toString();
            for (int i = 0; i < userIds.size(); i++) {
                String value = channelIds.get(i);
                if (value != null && value.equals(chId))
                    onlineParticipantUserIds.add(userIds.get(i));
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

    private String buildChannelIdKey(UserId userId) {
        return cacheService.buildKey(KeyPrefix.USER, userId.id().toString(), IdKey.CHANNEL_ID.getValue());
    }

    private String buildUserSessionLocationKey(UserId userId) {
        return cacheService.buildKey(KeyPrefix.USER_SESSION_LOCATION, userId.id().toString());
    }
}
