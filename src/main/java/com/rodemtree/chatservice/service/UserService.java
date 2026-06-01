package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.dto.domain.InviteCode;
import com.rodemtree.chatservice.dto.domain.User;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.projection.ConnectionCountProjection;
import com.rodemtree.chatservice.dto.projection.InviteCodeProjection;
import com.rodemtree.chatservice.dto.projection.UsernameProjection;
import com.rodemtree.chatservice.entity.UserEntity;
import com.rodemtree.chatservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(SessionService sessionService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<String> getUsername(UserId userId) {
        return userRepository.findUsernameByUserId(userId.id())
                .map(UsernameProjection::getUsername);
    }

    public Optional<UserId> getUserId(String username) {
        return userRepository.findByUsername(username)
                .map(entity -> new UserId(entity.getUserId()));
    }

    public Optional<User> getUser(InviteCode inviteCode) {
        return userRepository.findByConnectionInviteCode(inviteCode.code())
                .map(entity -> new User(new UserId(entity.getUserId()), entity.getUsername()));
    }

    public Optional<InviteCode> getInviteCode(UserId userId) {
        return userRepository.findInviteCodeByUserId(userId.id())
                .map(inviteCode -> new InviteCode(inviteCode.getConnectionInviteCode()));
    }

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
