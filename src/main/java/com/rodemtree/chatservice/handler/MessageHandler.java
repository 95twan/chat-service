package com.rodemtree.chatservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodemtree.chatservice.dto.Message;
import com.rodemtree.chatservice.entity.MessageEntity;
import com.rodemtree.chatservice.repository.MessageRepository;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MessageHandler extends TextWebSocketHandler {

    public static final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebSocketSessionManager webSocketSessionManager;
    private final MessageRepository messageRepository;

    public MessageHandler(WebSocketSessionManager webSocketSessionManager, MessageRepository messageRepository) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.messageRepository = messageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Connection established: {}", session.getId());

        ConcurrentWebSocketSessionDecorator concurrentWebSocketSessionDecorator = new ConcurrentWebSocketSessionDecorator(session, 5000, 100 * 1024);

        webSocketSessionManager.storeSession(concurrentWebSocketSessionDecorator);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error: [{}] from {}", exception.getMessage(), session.getId());
        webSocketSessionManager.terminateSession(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        log.info("Connection closed: [{}] from {}", status, session.getId());
        webSocketSessionManager.terminateSession(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession senderSession, TextMessage message) {
        log.info("Received message: [{}] from {}", message.getPayload(), senderSession.getId());
        String payload = message.getPayload();

        try {
            Message receivedMessage = objectMapper.readValue(payload, Message.class);

            messageRepository.save(new MessageEntity(receivedMessage.username(), receivedMessage.content()));

            webSocketSessionManager.getSessions().forEach(participantSession -> {
                if (!participantSession.getId().equals(senderSession.getId())) {
                    sendMessage(participantSession, receivedMessage);
                }
            });

        } catch (Exception e) {
            String errorMessage = "유효안 프로토콜이 아닙니다.";
            log.error("errorMessage payload: {} from {}", payload, senderSession.getId());
            sendMessage(senderSession, new Message("system", errorMessage));
        }
    }

    private void sendMessage(WebSocketSession session, Message message) {
        try {
            String msg = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(msg));
            log.info("Sent message: [{}] to {}", msg, session.getId());
        } catch (Exception e) {
            log.error("Failed to send message to {} error: {}", session.getId(), e.getMessage());
        }
    }
}
