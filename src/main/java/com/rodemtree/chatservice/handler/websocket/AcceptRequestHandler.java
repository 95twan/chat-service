package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.AcceptInviteRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.AcceptInviteNotification;
import com.rodemtree.chatservice.dto.websocket.outbound.AcceptInviteResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserConnectionService;
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
    private final ClientNotificationService clientNotificationService;


    @Override
    public void handle(WebSocketSession senderSession, AcceptInviteRequest request) {
        UserId acceptorUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        Pair<Optional<UserId>, String> result = userConnectionService.acceptInvite(acceptorUserId, request.getUsername());

        result.getFirst().ifPresentOrElse(inviterUserId -> {
            clientNotificationService.sendMessage(senderSession, acceptorUserId, new AcceptInviteResponse(request.getUsername()));
            String acceptorUsername = result.getSecond();
            clientNotificationService.sendMessage(inviterUserId, new AcceptInviteNotification(acceptorUsername)
            );
        }, () -> {
            String errorMessage = result.getSecond();
            clientNotificationService.sendMessage(senderSession, acceptorUserId, new ErrorResponse(MessageType.ACCEPT_INVITE_REQUEST, errorMessage));
        });
    }
}
