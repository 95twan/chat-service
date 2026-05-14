package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.entity.ChatUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUserEntity, Long> {
    Optional<ChatUserEntity> findByUsername(String username);
}
