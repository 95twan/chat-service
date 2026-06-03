package com.rodemtree.chatservice.handler;

import com.rodemtree.chatservice.constant.Constants;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.BaseRequest;
import com.rodemtree.chatservice.handler.websocket.RequestDispatcher;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import com.rodemtree.chatservice.util.JsonUtil;
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
public class WebSocketHandler extends TextWebSocketHandler {

    public static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
    private final JsonUtil jsonUtil;
    private final WebSocketSessionManager webSocketSessionManager;
    private final RequestDispatcher requestDispatcher;

    public WebSocketHandler(
            JsonUtil jsonUtil,
            WebSocketSessionManager webSocketSessionManager,
            RequestDispatcher requestDispatcher
    ) {
        this.jsonUtil = jsonUtil;
        this.webSocketSessionManager = webSocketSessionManager;
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Connection established: {}", session.getId());

        ConcurrentWebSocketSessionDecorator concurrentWebSocketSessionDecorator = new ConcurrentWebSocketSessionDecorator(session, 5000, 100 * 1024);
        UserId userId = (UserId) session.getAttributes().get(Constants.USER_ID.getValue());
        webSocketSessionManager.putSession(userId, concurrentWebSocketSessionDecorator);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error: [{}] from {}", exception.getMessage(), session.getId());
        UserId userId = (UserId) session.getAttributes().get(Constants.USER_ID.getValue());
        webSocketSessionManager.closeSession(userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        log.info("Connection closed: [{}] from {}", status, session.getId());
        UserId userId = (UserId) session.getAttributes().get(Constants.USER_ID.getValue());
        webSocketSessionManager.closeSession(userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession senderSession, TextMessage message) {
        String payload = message.getPayload();
        log.info("Received message: [{}] from {}", message.getPayload(), senderSession.getId());
        jsonUtil.fromJson(payload, BaseRequest.class)
                .ifPresent(msg -> requestDispatcher.dispatchRequest(senderSession, msg));
    }
}
