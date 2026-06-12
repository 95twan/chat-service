package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.CreateChannelRequest;
import com.rodemtree.chatservice.dto.websocket.inbound.FetchChannelInviteCodeRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.CreateChannelResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.FetchChannelInviteCodeResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.JoinChannelNotification;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.SessionService;
import com.rodemtree.chatservice.service.UserService;
import com.rodemtree.chatservice.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class FetchChannelInviteCodeRequestHandler implements BaseRequestHandler<FetchChannelInviteCodeRequest> {


    private static final Logger log = LoggerFactory.getLogger(FetchChannelInviteCodeRequestHandler.class);

    private final ChannelService channelService;
    private final WebSocketSessionManager webSocketSessionManager;


    @Override
    public void handle(WebSocketSession senderSession, FetchChannelInviteCodeRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        if (!channelService.isJoined(senderUserId, request.getChannelId())) {
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST, "Not joined the channel."));
            return;
        }
        channelService.getChannelInviteCode(request.getChannelId()).ifPresentOrElse(inviteCode -> {
            webSocketSessionManager.sendMessage(senderSession, new FetchChannelInviteCodeResponse(request.getChannelId(), inviteCode));
        }, () -> {
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST, "Fetch channel invite code failed."));
        });
    }
}
