package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.MessageSeqId;
import lombok.Getter;

@Getter
public class WriteMessageAck extends BaseMessage {

    private final Long serial;
    private final MessageSeqId messageSeqId;


    public WriteMessageAck(Long serial, MessageSeqId messageSeqId) {
        super(MessageType.WRITE_MESSAGE_ACK);
        this.serial = serial;
        this.messageSeqId = messageSeqId;
    }
}
