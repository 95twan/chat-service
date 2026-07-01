package com.rodemtree.chatservice.repository;

import com.rodemtree.chatservice.dto.projection.MessageInfoProjection;
import com.rodemtree.chatservice.entity.ChannelMessageSeqId;
import com.rodemtree.chatservice.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<MessageEntity, ChannelMessageSeqId> {

    @Query("select max(m.messageSequence) from MessageEntity m where m.channelId = :channelId")
    Optional<Long> findLastMessageSequenceByChannelId(@NonNull @Param("channelId") Long channelId);

    List<MessageInfoProjection> findMessageInfoByChannelIdAndMessageSequenceBetween(
            @NonNull Long channelId,
            @NonNull Long startMessageSequence,
            @NonNull Long endMessageSequence);
}
