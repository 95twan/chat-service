package com.rodemtree.chatservice.auth;

import com.rodemtree.chatservice.entity.ChatUserEntity;
import com.rodemtree.chatservice.repository.ChatUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ChatUserDetailsService implements UserDetailsService {


    private static final Logger log = LoggerFactory.getLogger(ChatUserDetailsService.class);
    private final ChatUserRepository chatUserRepository;

    public ChatUserDetailsService(ChatUserRepository chatUserRepository) {
        this.chatUserRepository = chatUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ChatUserEntity chatUserEntity = chatUserRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.info("User not found: {}", username);
                    return new UsernameNotFoundException("");
                });

        return new ChatUserDetails(chatUserEntity.getUserId(), chatUserEntity.getUsername(), chatUserEntity.getPassword());
    }
}
