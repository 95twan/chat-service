package com.rodemtree.chatservice.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

@Getter
@Entity
@Table(name = "message")
@IdClass(ChannelMessageSeqId.class)
public class MessageEntity extends BaseEntity {

    @Id
    @Column(name = "channel_id")
    private Long channelId;

    @Id
    @Column(name = "message_sequence")
    private Long messageSequence;


    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;

    @Column(name = "content", nullable = false)
    private String content;


    public MessageEntity() {
    }

    public MessageEntity(Long channelId, Long messageSequence, Long senderUserId, String content) {
        this.channelId = channelId;
        this.messageSequence = messageSequence;
        this.senderUserId = senderUserId;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MessageEntity that = (MessageEntity) o;
        return Objects.equals(channelId, that.channelId) && Objects.equals(messageSequence, that.messageSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, messageSequence);
    }

    @Override
    public String toString() {
        return "MessageEntity{channelId=%d, messageSequence=%d, senderUserId=%d, content='%s'}".formatted(channelId, messageSequence, senderUserId, content);
    }
}
