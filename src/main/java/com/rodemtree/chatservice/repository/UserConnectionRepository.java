package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.projection.InviterUserIdProjection;
import com.rodemtree.chatservice.dto.projection.UserConnectionStatusProjection;
import com.rodemtree.chatservice.dto.projection.UserIdUsernameProjection;
import com.rodemtree.chatservice.entity.UserConnectionEntity;
import com.rodemtree.chatservice.entity.UserConnectionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface UserConnectionRepository extends JpaRepository<UserConnectionEntity, UserConnectionId> {
    Optional<UserConnectionStatusProjection> findStatusByPartnerAUserIdAndPartnerBUserId(@NonNull Long partnerAUserId, @NonNull Long partnerBUserId);

    Optional<UserConnectionEntity> findByPartnerAUserIdAndPartnerBUserIdAndStatus(@NonNull Long partnerAUserId, @NonNull Long partnerBUserId, @NonNull UserConnectionStatus status);

    Optional<InviterUserIdProjection> findInviterUserIdByPartnerAUserIdAndPartnerBUserId(@NonNull Long partnerAUserId, @NonNull Long partnerBUserId);

    @Query(
            "SELECT u.partnerBUserId AS userId, userB.username As username " +
                    "FROM UserConnectionEntity u " +
                    "INNER JOIN UserEntity userB ON u.partnerBUserId = userB.userId " +
                    "WHERE u.partnerAUserId = :userId AND u.status = :status"
    )
    List<UserIdUsernameProjection> findConnectionsByPartnerAUserIdAndStatus(@Param("userId") @NonNull Long userId, @Param("status") @NonNull UserConnectionStatus status);

    @Query(
            "SELECT u.partnerAUserId AS userId, userA.username As username " +
                    "FROM UserConnectionEntity u " +
                    "INNER JOIN UserEntity userA ON u.partnerAUserId = userA.userId " +
                    "WHERE u.partnerBUserId = :userId AND u.status = :status"
    )
    List<UserIdUsernameProjection> findConnectionsByPartnerBUserIdAndStatus(@Param("userId") @NonNull Long userId, @Param("status") @NonNull UserConnectionStatus status);

}
