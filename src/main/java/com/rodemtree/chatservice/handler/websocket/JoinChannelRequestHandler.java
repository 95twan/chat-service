package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.JoinChannelRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.JoinChannelResponse;
import com.rodemtree.chatservice.service.ChannelService;
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
public class JoinChannelRequestHandler implements BaseRequestHandler<JoinChannelRequest> {


    private static final Logger log = LoggerFactory.getLogger(JoinChannelRequestHandler.class);

    private final ChannelService channelService;
    private final WebSocketSessionManager webSocketSessionManager;


    @Override
    public void handle(WebSocketSession senderSession, JoinChannelRequest request) {
        UserId joinUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        Pair<Optional<Channel>, ResultType> result;

        try {
            result = channelService.joinChannel(request.getInviteCode(), joinUserId);
        } catch (Exception ex) {
            log.error("Join channel failed. cause: {}", ex.getMessage());
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.JOIN_CHANNEL_REQUEST, ResultType.FAILED.getMessage()));
            return;
        }

        result.getFirst().ifPresentOrElse(channel -> {
            webSocketSessionManager.sendMessage(senderSession, new JoinChannelResponse(channel.channelId(), channel.title()));
        }, () -> {
            String errorMessage = result.getSecond().getMessage();
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.JOIN_CHANNEL_REQUEST, errorMessage));
        });
    }
}
