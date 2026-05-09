package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
}
