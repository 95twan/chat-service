package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import lombok.Getter;

@Getter
public class EnterChannelResponse extends BaseMessage {

    private final ChannelId channelId;
    private final String title;
    private final MessageSeqId lastReadMessageSeqId;
    private final MessageSeqId lastChannelMessageSeqId;

    public EnterChannelResponse(
            ChannelId channelId,
            String title,
            MessageSeqId lastReadMessageSeqId,
            MessageSeqId lastChannelMessageSeqId
    ) {
        super(MessageType.ENTER_CHANNEL_RESPONSE);
        this.channelId = channelId;
        this.title = title;
        this.lastReadMessageSeqId = lastReadMessageSeqId;
        this.lastChannelMessageSeqId = lastChannelMessageSeqId;
    }
}
