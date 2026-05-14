package com.rodemtree.chatservice.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "chat_user")
public class ChatUserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    public ChatUserEntity() {
    }

    public ChatUserEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChatUserEntity that = (ChatUserEntity) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override
    public String toString() {
        return "MessageEntity{userId=%d, username='%s', createdAt=%s, updatedAt=%s}"
                .formatted(userId, username, getCreatedAt(), getUpdatedAt());
    }
}
