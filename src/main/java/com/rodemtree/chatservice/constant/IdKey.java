package com.rodemtree.chatservice.constant;

import lombok.Getter;

@Getter
public enum IdKey {
    HTTP_SESSION_ID("HTTP_SESSION_ID"),
    USER_ID("USER_ID"),
    CHANNEL_ID("channel_id");

    private final String value;

    IdKey(String value) {
        this.value = value;
    }
}
