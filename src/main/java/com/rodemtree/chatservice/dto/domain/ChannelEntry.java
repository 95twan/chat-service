package com.rodemtree.chatservice.dto.domain;

public record ChannelEntry(String title, MessageSeqId lastReadMessageSeqId, MessageSeqId lastChannelMessageSeqId) {
}
