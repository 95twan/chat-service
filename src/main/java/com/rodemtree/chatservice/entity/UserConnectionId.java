package com.rodemtree.chatservice.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class UserConnectionId implements Serializable {

    private Long partnerAUserId;
    private Long partnerBUserId;

    public UserConnectionId() {
    }

    public UserConnectionId(Long partnerAUserId, Long partnerBUserId) {
        this.partnerAUserId = partnerAUserId;
        this.partnerBUserId = partnerBUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserConnectionId that = (UserConnectionId) o;
        return Objects.equals(partnerAUserId, that.partnerAUserId) && Objects.equals(partnerBUserId, that.partnerBUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partnerAUserId, partnerBUserId);
    }

    @Override
    public String toString() {
        return "UserConnectionId{partnerAUserId=%d, partnerBUserId=%d}".formatted(partnerAUserId, partnerBUserId);
    }
}
