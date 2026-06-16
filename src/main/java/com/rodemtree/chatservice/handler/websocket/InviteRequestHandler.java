package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.InviteRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.InviteNotification;
import com.rodemtree.chatservice.dto.websocket.outbound.InviteResponse;
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
public class InviteRequestHandler implements BaseRequestHandler<InviteRequest> {


    private static final Logger log = LoggerFactory.getLogger(InviteRequestHandler.class);

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;


    @Override
    public void handle(WebSocketSession senderSession, InviteRequest request) {
        UserId inviterUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        Pair<Optional<UserId>, String> result = userConnectionService.invite(inviterUserId, request.getUserInviteCode());
        result.getFirst().ifPresentOrElse(partnerUserId -> {
            String inviterUsername = result.getSecond();
            clientNotificationService.sendMessage(senderSession, inviterUserId, new InviteResponse(request.getUserInviteCode(), UserConnectionStatus.PENDING));
            clientNotificationService.sendMessage(partnerUserId, new InviteNotification(inviterUsername));
        }, () -> {
            String errorMessage = result.getSecond();
            clientNotificationService.sendMessage(senderSession, inviterUserId, new ErrorResponse(MessageType.INVITE_REQUEST, errorMessage));
        });

    }
}
