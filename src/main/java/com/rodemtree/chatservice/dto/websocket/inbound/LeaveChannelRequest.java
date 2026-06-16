package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class LeaveChannelRequest extends BaseRequest {

    @JsonCreator
    public LeaveChannelRequest() {
        super(MessageType.LEAVE_CHANNEL_REQUEST);
    }
}
