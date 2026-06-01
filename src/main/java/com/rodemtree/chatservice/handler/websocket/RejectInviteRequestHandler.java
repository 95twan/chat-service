package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.Constants;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.AcceptRequest;
import com.rodemtree.chatservice.dto.websocket.inbound.RejectInviteRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.AcceptNotification;
import com.rodemtree.chatservice.dto.websocket.outbound.AcceptResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.RejectInviteResponse;
import com.rodemtree.chatservice.service.UserConnectionService;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Component
@AllArgsConstructor
public class RejectInviteRequestHandler implements BaseRequestHandler<RejectInviteRequest> {

    private static final Logger log = LoggerFactory.getLogger(RejectInviteRequestHandler.class);

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager webSocketSessionManager;


    @Override
    public void handle(WebSocketSession senderSession, RejectInviteRequest request) {
        UserId rejectorUserId = (UserId) senderSession.getAttributes().get(Constants.USER_ID.getValue());
        Pair<Boolean, String> result = userConnectionService.rejectInvite(rejectorUserId, request.getUsername());

        if (result.getFirst()){
            webSocketSessionManager.sendMessage(senderSession, new RejectInviteResponse(request.getUsername(), UserConnectionStatus.REJECTED));
        } else {
            String errorMessage = result.getSecond();
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.REJECT_INVITE_REQUEST, errorMessage));
        }
    }
}
