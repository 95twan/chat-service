package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.entity.ChatUserEntity;
import com.rodemtree.chatservice.repository.ChatUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatUserService {

    private static final Logger log = LoggerFactory.getLogger(ChatUserService.class);
    private final SessionService sessionService;
    private final ChatUserRepository chatUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ChatUserService(SessionService sessionService, ChatUserRepository chatUserRepository, PasswordEncoder passwordEncoder) {
        this.sessionService = sessionService;
        this.chatUserRepository = chatUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserId addUser(String username, String password) {
        ChatUserEntity chatUserEntity = chatUserRepository.save(new ChatUserEntity(username, passwordEncoder.encode(password)));
        log.info("User registered. UserId: {}, Username: {}", chatUserEntity.getUserId(), chatUserEntity.getUsername());

        return new UserId(chatUserEntity.getUserId());
    }

    @Transactional
    public void removeUser() {
        String username = sessionService.getUsername();
        ChatUserEntity chatUserEntity = chatUserRepository.findByUsername(username).orElseThrow();
        chatUserRepository.deleteById(chatUserEntity.getUserId());
        log.info("User removed. UserId: {}, Username: {}", chatUserEntity.getUserId(), chatUserEntity.getUsername());
    }
}
