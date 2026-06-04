package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.dto.websocket.inbound.WriteMessageRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.MessageNotification;
import com.rodemtree.chatservice.entity.MessageEntity;
import com.rodemtree.chatservice.repository.MessageRepository;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class WriteMessageRequestHandler implements BaseRequestHandler<WriteMessageRequest> {

    private final WebSocketSessionManager webSocketSessionManager;
    private final MessageRepository messageRepository;


    @Override
    public void handle(WebSocketSession senderSession, WriteMessageRequest request) {
        MessageNotification receivedMessage = new MessageNotification(request.getUsername(), request.getContent());
        messageRepository.save(new MessageEntity(receivedMessage.getUsername(), receivedMessage.getContent()));

        webSocketSessionManager.getSessions().forEach(participantSession -> {
            if (!participantSession.getId().equals(senderSession.getId())) {
                webSocketSessionManager.sendMessage(participantSession, receivedMessage);
            }
        });
    }
}
