package com.rodemtree.chatservice.session;

import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.outbound.BaseMessage;
import com.rodemtree.chatservice.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);
    private final Map<UserId, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final JsonUtil jsonUtil;


    public List<WebSocketSession> getSessions() {
        return sessions.values().stream().toList();
    }

    public WebSocketSession getSession(UserId userId) {
        return sessions.get(userId);
    }

    public void putSession(UserId userId, WebSocketSession webSocketSession) {
        log.info("Store Session: {}", webSocketSession.getId());
        sessions.put(userId, webSocketSession);
    }

    public void closeSession(UserId userId) {
        try {
            WebSocketSession session = sessions.remove(userId);
            if (session != null) {
                log.info("Remove Session: {}", userId);
                session.close();
                log.info("Close Session: {}", userId);
            }
        } catch (Exception e) {
            log.error("Failed WebSocketSession close. userId: {}", userId);
        }
    }

    public void sendMessage(WebSocketSession session, BaseMessage message) {
        jsonUtil.toJson(message).ifPresent(msg -> {
            try {
                session.sendMessage(new TextMessage(msg));
                log.info("Sent message: [{}] to {}", msg, session.getId());
            } catch (Exception e) {
                log.error("Failed to send message. cause: {}", e.getMessage());
            }
        });
    }
}
