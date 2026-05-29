package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.projection.InviterUserIdProjection;
import com.rodemtree.chatservice.dto.projection.UserConnectionStatusProjection;
import com.rodemtree.chatservice.entity.UserConnectionEntity;
import com.rodemtree.chatservice.entity.UserConnectionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface UserConnectionRepository extends JpaRepository<UserConnectionEntity, UserConnectionId> {
    Optional<UserConnectionStatusProjection> findStatusByPartnerAUserIdAndPartnerBUserId(@NonNull Long partnerAUserId, @NonNull Long partnerBUserId);

    Optional<UserConnectionEntity> findByPartnerAUserIdAndPartnerBUserIdAndStatus(@NonNull Long partnerAUserId, @NonNull Long partnerBUserId, @NonNull UserConnectionStatus status);

    Optional<InviterUserIdProjection> findInviterUserIdByPartnerAUserIdAndPartnerBUserId(@NonNull Long partnerAUserId, @NonNull Long partnerBUserId);


}
