package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.Connection;
import com.rodemtree.chatservice.dto.domain.InviteCode;
import com.rodemtree.chatservice.dto.domain.User;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.projection.UserIdUsernameInviterUserIdProjection;
import com.rodemtree.chatservice.entity.UserConnectionEntity;
import com.rodemtree.chatservice.repository.UserConnectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserConnectionService {

    private static final Logger log = LoggerFactory.getLogger(UserConnectionService.class);

    private final UserService userService;
    private final UserConnectionLimitService userConnectionLimitService;
    private final UserConnectionRepository userConnectionRepository;


    @Transactional(readOnly = true)
    public List<Connection> getConnectionsByStatus(UserId userId, UserConnectionStatus status) {
        List<UserIdUsernameInviterUserIdProjection> userA = userConnectionRepository.findConnectionsByPartnerAUserIdAndStatus(userId.id(), status);
        List<UserIdUsernameInviterUserIdProjection> userB = userConnectionRepository.findConnectionsByPartnerBUserIdAndStatus(userId.id(), status);

        if (status == UserConnectionStatus.ACCEPTED) {
            return Stream.concat(userA.stream(), userB.stream())
                    .map(user -> new Connection(user.getUsername(), status)).toList();
        } else {
            return Stream.concat(userA.stream(), userB.stream())
                    .filter(item -> !item.getInviterUserId().equals(userId.id()))
                    .map(user -> new Connection(user.getUsername(), status)).toList();
        }
    }

    @Transactional(readOnly = true)
    public long countConnectionStatus(UserId senderUserId, List<UserId> partnerUserIds, UserConnectionStatus status) {
        List<Long> ids = partnerUserIds.stream().map(UserId::id).toList();
        return userConnectionRepository.countByPartnerAUserIdAndPartnerBUserIdInAndStatus(senderUserId.id(), ids, status) + userConnectionRepository.countByPartnerBUserIdAndPartnerAUserIdInAndStatus(senderUserId.id(), ids, status);
    }

    @Transactional
    public Pair<Optional<UserId>, String> invite(UserId inviterUserId, InviteCode inviteCode) {
        Optional<User> partner = userService.getUser(inviteCode);

        if (partner.isEmpty()) {
            log.info("Invalid invite code. {}, from {}", inviteCode, inviterUserId);
            return Pair.of(Optional.empty(), "Invalid invite code.");
        }

        UserId partnerUserId = partner.get().userId();
        String partnerUsername = partner.get().username();

        if (partnerUserId.equals(inviterUserId)) {
            return Pair.of(Optional.empty(), "Cannot self invite.");
        }

        UserConnectionStatus userConnectionStatus = getStatus(inviterUserId, partnerUserId);
        return switch (userConnectionStatus) {
            case NONE, DISCONNECTED -> {
                if (userService.getConnectionCount(inviterUserId).filter(count -> count >= userConnectionLimitService.getLimitConnection()).isPresent()) {
                    yield Pair.of(Optional.empty(), "Connection limit reached.");
                }
                Optional<String> inviterUsername = userService.getUsername(inviterUserId);
                if (inviterUsername.isEmpty()) {
                    log.warn("InviteRequest failed.");
                    yield Pair.of(Optional.empty(), "InviteRequest failed.");
                }
                try {
                    setStatus(inviterUserId, partnerUserId, UserConnectionStatus.PENDING);
                    yield Pair.of(Optional.of(partnerUserId), inviterUsername.get());
                } catch (Exception e) {
                    if (TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    }
                    log.error("Set pending failed. cause: {}", e.getMessage());
                    yield Pair.of(Optional.empty(), "Set pending failed.");
                }
            }
            case ACCEPTED -> Pair.of(Optional.empty(), "Already connected with " + partnerUsername);
            case PENDING, REJECTED -> {
                log.info("{} invites {} but does not deliver the invitation request.", inviterUserId, partnerUsername);
                yield Pair.of(Optional.empty(), "Already invited to " + partnerUsername);
            }
        };
    }

    @Transactional
    public Pair<Optional<UserId>, String> acceptInvite(UserId acceptorUserId, String inviterUsername) {
        Optional<UserId> userId = userService.getUserId(inviterUsername);
        if (userId.isEmpty()) {
            return Pair.of(Optional.empty(), "Invalid username.");
        }
        UserId inviterUserId = userId.get();

        if (acceptorUserId.equals(inviterUserId)) {
            return Pair.of(Optional.empty(), "Cannot self accept.");
        }

        if (getInviterUserId(acceptorUserId, inviterUserId).filter(invitationSenderUserId -> invitationSenderUserId.equals(inviterUserId)).isEmpty()) {
            return Pair.of(Optional.empty(), "Invalid username.");
        }

        UserConnectionStatus userConnectionStatus = getStatus(inviterUserId, acceptorUserId);
        if (userConnectionStatus == UserConnectionStatus.ACCEPTED) {
            return Pair.of(Optional.empty(), "Already connected.");
        }
        if (userConnectionStatus != UserConnectionStatus.PENDING) {
            return Pair.of(Optional.empty(), "Accept failed.");
        }

        Optional<String> acceptorUsername = userService.getUsername(acceptorUserId);
        if (acceptorUsername.isEmpty()) {
            log.error("Invalid userId. userId: {}", acceptorUserId);
            return Pair.of(Optional.empty(), "Accept failed.");
        }

        try {
            userConnectionLimitService.acceptInvite(acceptorUserId, inviterUserId);
            return Pair.of(Optional.of(inviterUserId), acceptorUsername.get());
        } catch (IllegalStateException ex) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            return Pair.of(Optional.empty(), ex.getMessage());
        } catch (Exception ex) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            log.error("Accept failed. cause: {}", ex.getMessage());
            return Pair.of(Optional.empty(), "Accept failed.");
        }
    }

    @Transactional
    public Pair<Boolean, String> rejectInvite(UserId rejectorUserId, String inviterUsername) {
        return userService.getUserId(inviterUsername)
                .filter(inviterUserId -> !inviterUserId.equals(rejectorUserId))
                .filter(inviterUserId ->
                        getInviterUserId(inviterUserId, rejectorUserId)
                                .filter(invitationSenderUserId -> invitationSenderUserId.equals(inviterUserId)).isPresent()
                )
                .filter(inviterUserId -> getStatus(inviterUserId, rejectorUserId) == UserConnectionStatus.PENDING)
                .map(inviterUserId -> {
                    try {
                        setStatus(inviterUserId, rejectorUserId, UserConnectionStatus.REJECTED);
                        return Pair.of(true, inviterUsername);
                    } catch (Exception ex) {
                        if (TransactionSynchronizationManager.isActualTransactionActive()) {
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        }
                        log.error("Set reject failed. cause: {}", ex.getMessage());
                        return Pair.of(false, "Reject failed.");
                    }
                })
                .orElse(Pair.of(false, "Reject failed."));
    }

    @Transactional
    public Pair<Boolean, String> disconnect(UserId senderUserId, String partnerUsername) {
        return userService.getUserId(partnerUsername)
                .filter(partnerUserId -> !senderUserId.equals(partnerUserId))
                .map(partnerUserId -> {
                    try {
                        UserConnectionStatus status = getStatus(senderUserId, partnerUserId);
                        if (status == UserConnectionStatus.ACCEPTED) {
                            userConnectionLimitService.disconnect(senderUserId, partnerUserId);
                            return Pair.of(true, partnerUsername);
                        } else if (status == UserConnectionStatus.REJECTED
                                && getInviterUserId(senderUserId, partnerUserId).filter(inviterUserId -> inviterUserId.equals(partnerUserId)).isPresent()) {
                            setStatus(senderUserId, partnerUserId, UserConnectionStatus.DISCONNECTED);
                            return Pair.of(true, partnerUsername);
                        }
                    } catch (Exception ex) {
                        if (TransactionSynchronizationManager.isActualTransactionActive()) {
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        }
                        log.error("Disconnect failed. cause: {}", ex.getMessage());
                    }
                    return Pair.of(false, "Disconnect failed.");
                })
                .orElse(Pair.of(false, "Disconnect failed."));
    }

    private Optional<UserId> getInviterUserId(UserId partnerAUserId, UserId partnerBUserId) {
        return userConnectionRepository.findInviterUserIdByPartnerAUserIdAndPartnerBUserId(Long.min(partnerAUserId.id(), partnerBUserId.id()), Long.max(partnerAUserId.id(), partnerBUserId.id()))
                .map(inviterUserId -> new UserId(inviterUserId.getInviterUserId()));
    }

    private UserConnectionStatus getStatus(UserId inviterUserId, UserId partnerUserId) {
        return userConnectionRepository.findStatusByPartnerAUserIdAndPartnerBUserId(Long.min(inviterUserId.id(), partnerUserId.id()), Long.max(inviterUserId.id(), partnerUserId.id()))
                .map(status -> UserConnectionStatus.valueOf(status.getStatus()))
                .orElse(UserConnectionStatus.NONE);
    }

    private void setStatus(UserId inviterUserId, UserId partnerUserId, UserConnectionStatus userConnectionStatus) {
        if (userConnectionStatus == UserConnectionStatus.ACCEPTED) {
            throw new IllegalArgumentException("Cannot set to accepted.");
        }

        userConnectionRepository.save(new UserConnectionEntity(
                Long.min(inviterUserId.id(), partnerUserId.id()),
                Long.max(inviterUserId.id(), partnerUserId.id()),
                userConnectionStatus,
                inviterUserId.id()
        ));
    }

}
