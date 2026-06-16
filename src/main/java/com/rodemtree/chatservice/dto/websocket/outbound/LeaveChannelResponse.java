package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class LeaveChannelResponse extends BaseMessage {

    public LeaveChannelResponse() {
        super(MessageType.LEAVE_CHANNEL_RESPONSE);
    }
}
