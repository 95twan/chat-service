package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.WriteMessage;
import com.rodemtree.chatservice.dto.websocket.outbound.MessageNotification;
import com.rodemtree.chatservice.service.MessageSeqIdGenerator;
import com.rodemtree.chatservice.service.MessageService;
import com.rodemtree.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class WriteMessageHandler implements BaseRequestHandler<WriteMessage> {

    private final UserService userService;
    private final MessageService messageService;
    private final MessageSeqIdGenerator messageSeqIdGenerator;


    @Override
    public void handle(WebSocketSession senderSession, WriteMessage request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        ChannelId channelId = request.getChannelId();
        String content = request.getContent();
        String senderUsername = userService.getUsername(senderUserId).orElse("unknown");
        messageSeqIdGenerator.getNext(channelId).ifPresent(seqId -> {
            messageService.sendMessage(
                    senderUserId,
                    channelId,
                    seqId,
                    request.getSerial(),
                    content,
                    new MessageNotification(channelId, seqId, senderUsername, content));
        });
    }
}
