package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.RejectInviteRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.RejectInviteResponse;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class RejectInviteRequestHandler implements BaseRequestHandler<RejectInviteRequest> {

    private static final Logger log = LoggerFactory.getLogger(RejectInviteRequestHandler.class);

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;


    @Override
    public void handle(WebSocketSession senderSession, RejectInviteRequest request) {
        UserId rejectorUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        Pair<Boolean, String> result = userConnectionService.rejectInvite(rejectorUserId, request.getUsername());

        if (result.getFirst()) {
            clientNotificationService.sendMessage(senderSession, rejectorUserId, new RejectInviteResponse(request.getUsername(), UserConnectionStatus.REJECTED));
        } else {
            String errorMessage = result.getSecond();
            clientNotificationService.sendMessage(senderSession, rejectorUserId, new ErrorResponse(MessageType.REJECT_INVITE_REQUEST, errorMessage));
        }
    }
}
