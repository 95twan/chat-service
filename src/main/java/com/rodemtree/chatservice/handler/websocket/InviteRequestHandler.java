package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.Constants;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.InviteRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.InviteNotification;
import com.rodemtree.chatservice.dto.websocket.outbound.InviteResponse;
import com.rodemtree.chatservice.service.UserConnectionService;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Component
public class InviteRequestHandler implements BaseRequestHandler<InviteRequest> {


    private static final Logger log = LoggerFactory.getLogger(InviteRequestHandler.class);

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager webSocketSessionManager;

    public InviteRequestHandler(UserConnectionService userConnectionService, WebSocketSessionManager webSocketSessionManager) {
        this.userConnectionService = userConnectionService;
        this.webSocketSessionManager = webSocketSessionManager;
    }

    @Override
    public void handle(WebSocketSession senderSession, InviteRequest request) {
        UserId inviterUserId = (UserId) senderSession.getAttributes().get(Constants.USER_ID.getValue());
        Pair<Optional<UserId>, String> result = userConnectionService.invite(inviterUserId, request.getUserInviteCode());
        result.getFirst().ifPresentOrElse(partnerUserId -> {
            String inviterUsername = result.getSecond();
            webSocketSessionManager.sendMessage(senderSession, new InviteResponse(request.getUserInviteCode(), UserConnectionStatus.PENDING));
            webSocketSessionManager.sendMessage(
                    webSocketSessionManager.getSession(partnerUserId), new InviteNotification(inviterUsername)
            );
        }, () -> {
            String errorMessage = result.getSecond();
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.INVITE_REQUEST, errorMessage));
        });

    }
}
