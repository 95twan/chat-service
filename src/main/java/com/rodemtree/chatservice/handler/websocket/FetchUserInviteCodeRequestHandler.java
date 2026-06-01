package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.Constants;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.FetchUserInviteCodeRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.FetchUserInviteCodeResponse;
import com.rodemtree.chatservice.service.UserService;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@AllArgsConstructor
public class FetchUserInviteCodeRequestHandler implements BaseRequestHandler<FetchUserInviteCodeRequest> {


    private static final Logger log = LoggerFactory.getLogger(FetchUserInviteCodeRequestHandler.class);

    private final UserService userService;
    private final WebSocketSessionManager webSocketSessionManager;


    @Override
    public void handle(WebSocketSession senderSession, FetchUserInviteCodeRequest request) {
        UserId userId = (UserId) senderSession.getAttributes().get(Constants.USER_ID.getValue());

        userService.getInviteCode(userId).ifPresentOrElse(inviteCode -> {
            webSocketSessionManager.sendMessage(senderSession, new FetchUserInviteCodeResponse(inviteCode));
        }, () -> {
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.FETCH_USER_INVITE_CODE_REQUEST, "Fetch user invite code failed."));
        });
    }
}
