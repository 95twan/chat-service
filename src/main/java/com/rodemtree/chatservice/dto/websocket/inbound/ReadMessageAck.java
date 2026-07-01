package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import lombok.Getter;

@Getter
public class ReadMessageAck extends BaseRequest {

    private final ChannelId channelId;
    private final MessageSeqId messageSeqId;

    @JsonCreator
    public ReadMessageAck(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("messageSeqId") MessageSeqId messageSeqId
    ) {
        super(MessageType.READ_MESSAGE_ACK);
        this.channelId = channelId;
        this.messageSeqId = messageSeqId;
    }
}
