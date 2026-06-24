package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.KeyPrefix;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import com.rodemtree.chatservice.dto.domain.User;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.projection.ConnectionCountProjection;
import com.rodemtree.chatservice.dto.projection.UsernameProjection;
import com.rodemtree.chatservice.repository.UserRepository;
import com.rodemtree.chatservice.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final JsonUtil jsonUtil;
    private final long TTL = 3600;

    @Transactional(readOnly = true)
    public Optional<String> getUsername(UserId userId) {
        String key = cacheService.buildKey(KeyPrefix.USERNAME, userId.id().toString());
        Optional<String> cachedUserName = cacheService.get(key);
        if (cachedUserName.isPresent()) {
            return cachedUserName;
        }
        Optional<String> fromDB = userRepository.findUsernameByUserId(userId.id()).map(UsernameProjection::getUsername);
        fromDB.ifPresent(userName -> cacheService.set(key, userName, TTL));
        return fromDB;
    }

    @Transactional(readOnly = true)
    public Optional<UserId> getUserId(String username) {
        String key = cacheService.buildKey(KeyPrefix.USER_ID, username);
        Optional<String> cachedUserId = cacheService.get(key);
        if (cachedUserId.isPresent()) {
            return Optional.of(new UserId(Long.valueOf(cachedUserId.get())));
        }
        Optional<UserId> fromDB = userRepository.findUserIdByUsername(username)
                .map(projection -> new UserId(projection.getUserId()));
        fromDB.ifPresent(userId -> cacheService.set(key, userId.id().toString(), TTL));
        return fromDB;
    }

    @Transactional(readOnly = true)
    public List<UserId> getUserIds(List<String> usernames) {
        return userRepository.findUserIdsByUsernameIn(usernames).stream()
                .map(projection -> new UserId(projection.getUserId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUser(InviteCode inviteCode) {
        String key = cacheService.buildKey(KeyPrefix.USER, inviteCode.code());
        Optional<String> cachedUser = cacheService.get(key);
        if (cachedUser.isPresent()) {
            return jsonUtil.fromJson(cachedUser.get(), User.class);
        }
        Optional<User> fromDB = userRepository.findByInviteCode(inviteCode.code())
                .map(entity -> new User(new UserId(entity.getUserId()), entity.getUsername()));
        ;
        fromDB.flatMap(jsonUtil::toJson).ifPresent(json -> cacheService.set(key, json, TTL));
        return fromDB;
    }

    @Transactional(readOnly = true)
    public Optional<InviteCode> getInviteCode(UserId userId) {
        String key = cacheService.buildKey(KeyPrefix.USER_INVITE_CODE, userId.id().toString());
        Optional<String> cachedInviteCode = cacheService.get(key);
        if (cachedInviteCode.isPresent()) {
            return Optional.of(new InviteCode(cachedInviteCode.get()));
        }
        Optional<InviteCode> fromDB = userRepository.findInviteCodeByUserId(userId.id())
                .map(projection -> new InviteCode(projection.getInviteCode()));
        fromDB.ifPresent(inviteCode -> cacheService.set(key, inviteCode.code(), TTL));
        return fromDB;
    }

    @Transactional(readOnly = true)
    public Optional<Integer> getConnectionCount(UserId userId) {
        return userRepository.findConnectionCountByUserId(userId.id())
                .map(ConnectionCountProjection::getConnectionCount);
    }

}
