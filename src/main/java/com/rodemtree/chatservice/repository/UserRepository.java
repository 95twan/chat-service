package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.dto.projection.*;
import com.rodemtree.chatservice.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserIdProjection> findUserIdByUsername(@NonNull String username);

    List<UserIdProjection> findUserIdsByUsernameIn(@NonNull Collection<String> usernames);

    List<UserIdUsernameProjection> findUserIdUsernamesByUserIdIn(@NonNull Collection<Long> userIds);

    Optional<UsernameProjection> findUsernameByUserId(@NonNull Long userId);

    Optional<UserEntity> findByInviteCode(@NonNull String inviteCode);

    Optional<InviteCodeProjection> findInviteCodeByUserId(@NonNull Long userId);

    Optional<ConnectionCountProjection> findConnectionCountByUserId(@NonNull Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findForUpdateByUserId(@NonNull Long userId);
}
