package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.constant.UserConnectionStatus;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.CreateChannelRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.*;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.UserService;
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
public class CreateChannelRequestHandler implements BaseRequestHandler<CreateChannelRequest> {


    private static final Logger log = LoggerFactory.getLogger(CreateChannelRequestHandler.class);

    private final ChannelService channelService;
    private final UserService userService;
    private final WebSocketSessionManager webSocketSessionManager;


    @Override
    public void handle(WebSocketSession senderSession, CreateChannelRequest request) {
        UserId creatorUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        Optional<UserId> participantUserId = userService.getUserId(request.getParticipantUsername());
        if (participantUserId.isEmpty()) {
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.CREATE_CHANNEL_REQUEST, ResultType.NOT_FOUND.getMessage()));
            return;
        }

        UserId partnerUserId = participantUserId.get();
        Pair<Optional<Channel>, ResultType> result;
        try {
            result = channelService.createChannel(creatorUserId, partnerUserId, request.getTitle());
        } catch (Exception ex) {
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.CREATE_CHANNEL_REQUEST, ResultType.FAILED.getMessage()));
            return;
        }
        result.getFirst().ifPresentOrElse(channel -> {
            webSocketSessionManager.sendMessage(senderSession, new CreateChannelResponse(channel.channelId(), channel.title()));
            webSocketSessionManager.sendMessage(
                    webSocketSessionManager.getSession(partnerUserId), new JoinChannelNotification(channel.channelId(), channel.title())
            );
        }, () -> {
            String errorMessage = result.getSecond().getMessage();
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.CREATE_CHANNEL_REQUEST, errorMessage));
        });

    }
}
