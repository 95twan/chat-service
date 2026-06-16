package com.rodemtree.chatservice.dto.websocket.outbound;

import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.Channel;
import lombok.Getter;

import java.util.List;

@Getter
public class FetchChannelsResponse extends BaseMessage {

    private final List<Channel> channels;

    public FetchChannelsResponse(List<Channel> channels) {
        super(MessageType.FETCH_CHANNELS_RESPONSE);
        this.channels = channels;
    }
}
