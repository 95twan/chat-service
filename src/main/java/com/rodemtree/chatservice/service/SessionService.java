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
import java.util.concurrent.TimeUnit;

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

    public boolean isOnline(UserId userId, ChannelId channelId) {
        String channelIdKey = buildChannelIdKey(userId);
        try {
            String activeChannelId = stringRedisTemplate.opsForValue().get(channelIdKey);
            if (activeChannelId != null && activeChannelId.equals(channelId.id().toString())) {
                return true;
            }
        } catch (Exception ex) {
            log.error("Redis get failed. key: {}, cause: {}", channelIdKey, ex.getMessage());
        }
        return false;
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
        return "%s:%d:%s".formatted(NAMESPACE, userId.id(), IdKey.CHANNEL_ID);
    }
}
