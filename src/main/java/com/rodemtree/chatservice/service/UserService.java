package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.dto.domain.InviteCode;
import com.rodemtree.chatservice.dto.domain.User;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.projection.ConnectionCountProjection;
import com.rodemtree.chatservice.dto.projection.UsernameProjection;
import com.rodemtree.chatservice.entity.UserEntity;
import com.rodemtree.chatservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<String> getUsername(UserId userId) {
        return userRepository.findUsernameByUserId(userId.id())
                .map(UsernameProjection::getUsername);
    }

    @Transactional(readOnly = true)
    public Optional<UserId> getUserId(String username) {
        return userRepository.findUserIdByUsername(username)
                .map(projection -> new UserId(projection.getUserId()));
    }

    @Transactional(readOnly = true)
    public List<UserId> getUserIds(List<String> usernames) {
        return userRepository.findUserIdsByUsernameIn(usernames).stream()
                .map(userId -> new UserId(userId.getUserId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUser(InviteCode inviteCode) {
        return userRepository.findByInviteCode(inviteCode.code())
                .map(entity -> new User(new UserId(entity.getUserId()), entity.getUsername()));
    }

    @Transactional(readOnly = true)
    public Optional<InviteCode> getInviteCode(UserId userId) {
        return userRepository.findInviteCodeByUserId(userId.id())
                .map(inviteCode -> new InviteCode(inviteCode.getInviteCode()));
    }

    @Transactional(readOnly = true)
    public Optional<Integer> getConnectionCount(UserId userId) {
        return userRepository.findConnectionCountByUserId(userId.id())
                .map(ConnectionCountProjection::getConnectionCount);
    }

    @Transactional
    public UserId addUser(String username, String password) {
        UserEntity userEntity = userRepository.save(new UserEntity(username, passwordEncoder.encode(password)));
        log.info("User registered. UserId: {}, Username: {}", userEntity.getUserId(), userEntity.getUsername());

        return new UserId(userEntity.getUserId());
    }

    @Transactional
    public void removeUser() {
        String username = sessionService.getUsername();
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow();
        userRepository.deleteById(userEntity.getUserId());
        log.info("User removed. UserId: {}, Username: {}", userEntity.getUserId(), userEntity.getUsername());
    }
}
