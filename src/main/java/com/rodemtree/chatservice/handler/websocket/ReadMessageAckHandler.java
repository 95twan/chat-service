package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.ReadMessageAck;
import com.rodemtree.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ReadMessageAckHandler implements BaseRequestHandler<ReadMessageAck> {

    private final MessageService messageService;

    @Override
    public void handle(WebSocketSession senderSession, ReadMessageAck request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        ChannelId channelId = request.getChannelId();
        messageService.updateLastReadMessageSeq(senderUserId, channelId, request.getMessageSeqId());
    }
}
