package com.rodemtree.chatservice.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

@Getter
@Entity
@Table(name = "user_channel")
@IdClass(UserChannelId.class)
public class UserChannelEntity extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "last_read_message_seq", nullable = false)
    private Long lastReadMessageSeq;

    public UserChannelEntity() {
    }

    public UserChannelEntity(Long userId, Long channelId, Long lastReadMessageSeq) {
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadMessageSeq = lastReadMessageSeq;
    }

    public void setLastReadMessageSeq(Long lastReadMessageSeq) {
        this.lastReadMessageSeq = lastReadMessageSeq;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserChannelEntity that = (UserChannelEntity) o;
        return Objects.equals(userId, that.userId) && Objects.equals(channelId, that.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, channelId);
    }

    @Override
    public String toString() {
        return "UserChannelEntity{userId=%d, channelId=%d, lastReadMessageSeq=%d}"
                .formatted(userId, channelId, lastReadMessageSeq);
    }
}
