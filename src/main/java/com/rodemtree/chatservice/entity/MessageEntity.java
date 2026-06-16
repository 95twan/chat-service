package com.rodemtree.chatservice.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

@Getter
@Entity
@Table(name = "message")
public class MessageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_sequence")
    private Long messageSequence;

    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;

    @Column(name = "content", nullable = false)
    private String content;


    public MessageEntity() {
    }

    public MessageEntity(Long senderUserId, String content) {
        this.senderUserId = senderUserId;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MessageEntity that = (MessageEntity) o;
        return Objects.equals(messageSequence, that.messageSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageSequence);
    }

    @Override
    public String toString() {
        return "MessageEntity{messageSequence=%d, senderUserId=%d, content='%s', createdAt=%s, updatedAt=%s}"
                .formatted(messageSequence, senderUserId, content, getCreatedAt(), getUpdatedAt());
    }
}
