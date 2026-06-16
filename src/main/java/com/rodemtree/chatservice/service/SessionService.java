package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository<? extends Session> httpSessionRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final long TTL = 300;


    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public List<UserId> getOnlineParticipants(ChannelId channelId, List<UserId> userIds) {
        List<String> channelIdKeys = userIds.stream().map(this::buildChannelIdKey).toList();
        try {
            List<String> channelIds = stringRedisTemplate.opsForValue().multiGet(channelIdKeys);
            if (channelIds != null) {
                List<UserId> onlineParticipants = new ArrayList<>(channelIds.size());
                String chId = channelId.id().toString();
                for (int i = 0; i < userIds.size(); i++) {
                    String value = channelIds.get(i);
                    if (value != null && value.equals(chId)) {
                        onlineParticipants.add(userIds.get(i));
                    }
                }
                return onlineParticipants;
            }
        } catch (Exception ex) {
            log.error("Redis get failed. key: {}, cause: {}", channelIdKeys, ex.getMessage());
        }
        return Collections.emptyList();
    }

    public boolean setActiveChannel(UserId userId, ChannelId channelId) {
        String channelIdKey = buildChannelIdKey(userId);
        try {
            stringRedisTemplate.opsForValue().set(channelIdKey, channelId.id().toString(), TTL, TimeUnit.SECONDS);
            return true;
        } catch (Exception ex) {
            log.error("Redis set failed. key: {}, channelId: {}, cause: {}", channelIdKey, channelId.id(), ex.getMessage());
            return false;
        }
    }

    public boolean removeActiveChannel(UserId userId) {
        String channelIdKey = buildChannelIdKey(userId);
        try {
            stringRedisTemplate.delete(channelIdKey);
            return true;
        } catch (Exception ex) {
            log.error("Redis delete failed. key: {}, cause: {}", channelIdKey, ex.getMessage());
            return false;
        }
    }

    public void refreshTTL(UserId userId, String httpSessionId) {
        String channelIdKey = buildChannelIdKey(userId);
        try {
            Session httpSession = httpSessionRepository.findById(httpSessionId);
            if (httpSession != null) {
                httpSession.setLastAccessedTime(Instant.now());
                stringRedisTemplate.expire(channelIdKey, TTL, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            log.error("Redis expire failed. key: {}, cause: {}", channelIdKey, ex.getMessage());
        }
    }

    private String buildChannelIdKey(UserId userId) {
        String NAMESPACE = "message:user";
        return "%s:%d:%s".formatted(NAMESPACE, userId.id(), IdKey.CHANNEL_ID.getValue());
    }
}
