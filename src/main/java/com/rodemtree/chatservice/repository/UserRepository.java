package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.dto.projection.InviteCodeProjection;
import com.rodemtree.chatservice.dto.projection.UsernameProjection;
import com.rodemtree.chatservice.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(@NonNull String username);

    Optional<UsernameProjection> findUsernameByUserId(@NonNull Long userId);

    Optional<UserEntity> findByConnectionInviteCode(@NonNull String connectionInviteCode);

    Optional<InviteCodeProjection> findInviteCodeByUserId(@NonNull Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findForUpdateByUserId(@NonNull Long userId);
}
