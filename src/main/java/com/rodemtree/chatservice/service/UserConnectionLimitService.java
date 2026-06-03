package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.entity.UserConnectionEntity;
import com.rodemtree.chatservice.entity.UserEntity;
import com.rodemtree.chatservice.repository.UserConnectionRepository;
import com.rodemtree.chatservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
public class UserConnectionLimitService {

    private final UserRepository userRepository;
    private final UserConnectionRepository userConnectionRepository;

    private int limitConnection = 1_000;

    public UserConnectionLimitService(UserRepository userRepository, UserConnectionRepository userConnectionRepository) {
        this.userRepository = userRepository;
        this.userConnectionRepository = userConnectionRepository;
    }

    public int getLimitConnection() {
        return limitConnection;
    }

    public void setLimitConnection(int limitConnection) {
        this.limitConnection = limitConnection;
    }

    @Transactional
    public void accept(UserId acceptorUserId, UserId inviterUserId) {
        Long firstUserId = Long.min(acceptorUserId.id(), inviterUserId.id());
        Long secondUserId = Long.max(acceptorUserId.id(), inviterUserId.id());

        UserEntity firstUserEntity = userRepository.findForUpdateByUserId(firstUserId)
                .orElseThrow(() -> new EntityNotFoundException("Invalid user ID: " + firstUserId));
        UserEntity secondUserEntity = userRepository.findForUpdateByUserId(secondUserId).
                orElseThrow(() -> new EntityNotFoundException("Invalid user ID: " + secondUserId));

        UserConnectionEntity userConnectionEntity = userConnectionRepository.findByPartnerAUserIdAndPartnerBUserIdAndStatus(firstUserId, secondUserId, UserConnectionStatus.PENDING)
                .orElseThrow(() -> new EntityNotFoundException("Invalid status"));

        Function<Long, String> getErrorMessage = userId -> userId.equals(acceptorUserId.id()) ? "Connection limit reached." : "Connection limit reached by the other user.";

        int firstConnectionCount = firstUserEntity.getConnectionCount();
        if (firstConnectionCount >= limitConnection) {
            throw new IllegalStateException(getErrorMessage.apply(firstUserId));
        }
        int secondConnectionCount = secondUserEntity.getConnectionCount();
        if (secondConnectionCount >= limitConnection) {
            throw new IllegalStateException(getErrorMessage.apply(secondUserId));
        }

        firstUserEntity.setConnectionCount(firstConnectionCount + 1);
        secondUserEntity.setConnectionCount(secondConnectionCount + 1);
        userConnectionEntity.setStatus(UserConnectionStatus.ACCEPTED);
    }

    @Transactional
    public void disconnect(UserId senderUserId, UserId partnerUserId) {
        Long firstUserId = Long.min(senderUserId.id(), partnerUserId.id());
        Long secondUserId = Long.max(senderUserId.id(), partnerUserId.id());

        UserEntity firstUserEntity = userRepository.findForUpdateByUserId(firstUserId)
                .orElseThrow(() -> new EntityNotFoundException("Invalid user ID: " + firstUserId));
        UserEntity secondUserEntity = userRepository.findForUpdateByUserId(secondUserId).
                orElseThrow(() -> new EntityNotFoundException("Invalid user ID: " + secondUserId));

        UserConnectionEntity userConnectionEntity = userConnectionRepository.findByPartnerAUserIdAndPartnerBUserIdAndStatus(firstUserId, secondUserId, UserConnectionStatus.ACCEPTED)
                .orElseThrow(() -> new EntityNotFoundException("Invalid status"));

        int firstConnectionCount = firstUserEntity.getConnectionCount();
        if (firstConnectionCount <= 0) {
            throw new IllegalStateException("Count is already zero. userId; " + firstUserId);
        }
        int secondConnectionCount = secondUserEntity.getConnectionCount();
        if (secondConnectionCount <= 0) {
            throw new IllegalStateException("Count is already zero. userId; " + secondUserId);
        }

        firstUserEntity.setConnectionCount(firstConnectionCount - 1);
        secondUserEntity.setConnectionCount(secondConnectionCount - 1);
        userConnectionEntity.setStatus(UserConnectionStatus.DISCONNECTED);
    }
}
