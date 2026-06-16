package com.rodemtree.chatservice.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class UserChannelId implements Serializable {

    private Long userId;
    private Long channelId;

    public UserChannelId() {
    }

    public UserChannelId(Long userId, Long channelId) {
        this.userId = userId;
        this.channelId = channelId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserChannelId that = (UserChannelId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(channelId, that.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, channelId);
    }

    @Override
    public String toString() {
        return "ChannelId{userId=%d, channelId=%d}".formatted(userId, channelId);
    }
}
