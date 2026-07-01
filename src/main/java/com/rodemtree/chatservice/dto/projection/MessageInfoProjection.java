package com.rodemtree.chatservice.dto.projection;

public interface MessageInfoProjection {
    Long getMessageSequence();

    Long getSenderUserId();

    String getContent();
}
