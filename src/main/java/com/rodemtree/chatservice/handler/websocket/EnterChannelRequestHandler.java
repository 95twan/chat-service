package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.EnterChannelRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.EnterChannelResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EnterChannelRequestHandler implements BaseRequestHandler<EnterChannelRequest> {


    private static final Logger log = LoggerFactory.getLogger(EnterChannelRequestHandler.class);

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;


    @Override
    public void handle(WebSocketSession senderSession, EnterChannelRequest request) {
        UserId enterUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        Pair<Optional<String>, ResultType> result = channelService.enterChannel(enterUserId, request.getChannelId());

        result.getFirst().ifPresentOrElse(title -> {
            clientNotificationService.sendMessage(senderSession, enterUserId, new EnterChannelResponse(request.getChannelId(), title));
        }, () -> {
            String errorMessage = result.getSecond().getMessage();
            clientNotificationService.sendMessage(senderSession, enterUserId, new ErrorResponse(MessageType.ENTER_CHANNEL_REQUEST, errorMessage));
        });
    }
}
