package com.rodemtree.chatservice.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodemtree.chatservice.constant.MessageType;
import lombok.Getter;

@Getter
public class CreateChannelRequest extends BaseRequest {

    private final String title;
    private final String participantUsername;

    @JsonCreator
    public CreateChannelRequest(
            @JsonProperty("title") String title,
            @JsonProperty("participantUsername") String participantUsername
    ) {
        super(MessageType.CREATE_CHANNEL_REQUEST);
        this.title = title;
        this.participantUsername = participantUsername;
    }
}
