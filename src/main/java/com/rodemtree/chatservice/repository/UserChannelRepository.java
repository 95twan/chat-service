package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.dto.projection.ChannelProjection;
import com.rodemtree.chatservice.dto.projection.UserIdProjection;
import com.rodemtree.chatservice.entity.UserChannelEntity;
import com.rodemtree.chatservice.entity.UserChannelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserChannelRepository extends JpaRepository<UserChannelEntity, UserChannelId> {

    boolean existsByUserIdAndChannelId(@NonNull Long userId, @NonNull Long channelId);

    List<UserIdProjection> findUserIdsByChannelId(@NonNull Long channelId);

    @Query(
            "SELECT c.channelId AS channelId, c.title AS title, c.headCount AS headCount " +
                    "FROM UserChannelEntity uc " +
                    "JOIN ChannelEntity c ON uc.channelId = c.id " +
                    "WHERE uc.userId = :userId"
    )
    List<ChannelProjection> findChannelsByUserId(@NonNull @Param("userId") Long userId);

    void deleteByUserIdAndChannelId(@NonNull Long userId, @NonNull Long channelId);
}
