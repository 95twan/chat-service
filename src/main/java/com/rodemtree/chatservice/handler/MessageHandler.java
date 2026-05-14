package com.rodemtree.chatservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodemtree.chatservice.constant.Constants;
import com.rodemtree.chatservice.dto.domain.Message;
import com.rodemtree.chatservice.dto.websocket.inbound.BaseRequest;
import com.rodemtree.chatservice.dto.websocket.inbound.KeepAliveRequest;
import com.rodemtree.chatservice.dto.websocket.inbound.MessageRequest;
import com.rodemtree.chatservice.entity.MessageEntity;
import com.rodemtree.chatservice.repository.MessageRepository;
import com.rodemtree.chatservice.service.SessionService;
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
    private final SessionService sessionService;

    public MessageHandler(WebSocketSessionManager webSocketSessionManager, MessageRepository messageRepository, SessionService sessionService) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.messageRepository = messageRepository;
        this.sessionService = sessionService;
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
        String payload = message.getPayload();
        log.info("Received message: [{}] from {}", message.getPayload(), senderSession.getId());

        try {
            BaseRequest baseRequest = objectMapper.readValue(payload, BaseRequest.class);

            if (baseRequest instanceof MessageRequest messageRequest) {
                Message receivedMessage = new Message(messageRequest.getUsername(), messageRequest.getContent());
                messageRepository.save(new MessageEntity(receivedMessage.username(), receivedMessage.content()));

                webSocketSessionManager.getSessions().forEach(participantSession -> {
                    if (!participantSession.getId().equals(senderSession.getId())) {
                        sendMessage(participantSession, receivedMessage);
                    }
                });
            } else if (baseRequest instanceof KeepAliveRequest keepAliveRequest) {
                sessionService.refreshTTL((String) senderSession.getAttributes().get(Constants.HTTP_SESSION_ID.getValue()));
            }

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
