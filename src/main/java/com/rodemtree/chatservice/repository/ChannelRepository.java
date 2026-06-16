package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.dto.projection.ChannelProjection;
import com.rodemtree.chatservice.dto.projection.ChannelTitleProjection;
import com.rodemtree.chatservice.dto.projection.InviteCodeProjection;
import com.rodemtree.chatservice.entity.ChannelEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.lang.NonNull;

import java.util.Optional;


public interface ChannelRepository extends JpaRepository<ChannelEntity, Long> {

    Optional<ChannelTitleProjection> findChannelTitleByChannelId(@NonNull Long channelId);

    Optional<InviteCodeProjection> findChannelInviteCodeByChannelId(@NonNull Long channelId);

    Optional<ChannelProjection> findChannelByInviteCode(@NonNull String inviteCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChannelEntity> findChannelForUpdateByChannelId(@NonNull Long channelId);


}
