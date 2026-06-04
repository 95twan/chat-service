package com.rodemtree.chatservice.dto.domain;

import com.rodemtree.chatservice.constant.UserConnectionStatus;

public record Connection(
        String username,
        UserConnectionStatus status
) {
}
