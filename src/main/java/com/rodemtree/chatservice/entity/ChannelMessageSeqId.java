package com.rodemtree.chatservice.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@NoArgsConstructor
public class ChannelMessageSeqId {

    private Long channelId;
    private Long messageSequence;

    public ChannelMessageSeqId(Long channelId, Long messageSequence) {
        this.channelId = channelId;
        this.messageSequence = messageSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChannelMessageSeqId that = (ChannelMessageSeqId) o;
        return Objects.equals(channelId, that.channelId) && Objects.equals(messageSequence, that.messageSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, messageSequence);
    }

    @Override
    public String toString() {
        return "ChannelMessageSequenceId{channelId=%d, messageSequence=%d}".formatted(channelId, messageSequence);
    }
}
