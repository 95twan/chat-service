package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.dto.projection.UserIdProjection;
import com.rodemtree.chatservice.entity.UserChannelId;
import com.rodemtree.chatservice.entity.UserChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserChannelRepository extends JpaRepository<UserChannelEntity, UserChannelId> {

    boolean existsByUserIdAndChannelId(@NonNull Long userId, @NonNull Long channelId);

    List<UserIdProjection> findUserIdsByChannelId(@NonNull Long channelId);
}
