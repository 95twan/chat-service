package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.Constants;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.AcceptInviteRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.AcceptInviteNotification;
import com.rodemtree.chatservice.dto.websocket.outbound.AcceptInviteResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.service.UserConnectionService;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AcceptRequestHandler implements BaseRequestHandler<AcceptInviteRequest> {

    private static final Logger log = LoggerFactory.getLogger(AcceptRequestHandler.class);

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager webSocketSessionManager;


    @Override
    public void handle(WebSocketSession senderSession, AcceptInviteRequest request) {
        UserId acceptorUserId = (UserId) senderSession.getAttributes().get(Constants.USER_ID.getValue());
        Pair<Optional<UserId>, String> result = userConnectionService.acceptInvite(acceptorUserId, request.getUsername());

        result.getFirst().ifPresentOrElse(inviterUserId -> {
            webSocketSessionManager.sendMessage(senderSession, new AcceptInviteResponse(request.getUsername()));
            String acceptorUsername = result.getSecond();
            webSocketSessionManager.sendMessage(
                    webSocketSessionManager.getSession(inviterUserId), new AcceptInviteNotification(acceptorUsername)
            );
        }, () -> {
            String errorMessage = result.getSecond();
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.ACCEPT_INVITE_REQUEST, errorMessage));
        });
    }
}
