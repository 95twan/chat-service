package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.CreateChannelRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.CreateChannelResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.JoinChannelNotification;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.UserService;
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
public class CreateChannelRequestHandler implements BaseRequestHandler<CreateChannelRequest> {


    private static final Logger log = LoggerFactory.getLogger(CreateChannelRequestHandler.class);

    private final ChannelService channelService;
    private final UserService userService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handle(WebSocketSession senderSession, CreateChannelRequest request) {
        UserId creatorUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        List<UserId> participantIds = userService.getUserIds(request.getParticipantUsernames());

        if (participantIds.isEmpty()) {
            clientNotificationService.sendMessage(senderSession, creatorUserId, new ErrorResponse(MessageType.CREATE_CHANNEL_REQUEST, ResultType.NOT_FOUND.getMessage()));
            return;
        }

        Pair<Optional<Channel>, ResultType> result;
        try {
            result = channelService.createChannel(creatorUserId, participantIds, request.getTitle());
        } catch (Exception ex) {
            clientNotificationService.sendMessage(senderSession, creatorUserId, new ErrorResponse(MessageType.CREATE_CHANNEL_REQUEST, ResultType.FAILED.getMessage()));
            return;
        }

        if (result.getFirst().isEmpty()) {
            String errorMessage = result.getSecond().getMessage();
            clientNotificationService.sendMessage(senderSession, creatorUserId, new ErrorResponse(MessageType.CREATE_CHANNEL_REQUEST, errorMessage));
            return;
        }

        Channel channel = result.getFirst().get();

        clientNotificationService.sendMessage(senderSession, creatorUserId, new CreateChannelResponse(channel.channelId(), channel.title()));

        participantIds.forEach(partnerUserId ->
                CompletableFuture.runAsync(() ->
                        clientNotificationService.sendMessage(partnerUserId, new JoinChannelNotification(channel.channelId(), channel.title()))
                )
        );
    }
}
