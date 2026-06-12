package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.dto.projection.ChannelTitleProjection;
import com.rodemtree.chatservice.dto.projection.InviteCodeProjection;
import com.rodemtree.chatservice.entity.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;


public interface ChannelRepository extends JpaRepository<ChannelEntity, Long> {

    Optional<ChannelTitleProjection> findChannelTitleByChannelId(@NonNull Long channelId);

    Optional<InviteCodeProjection> findChannelInviteCodeByChannelId(@NonNull Long channelId);
}
