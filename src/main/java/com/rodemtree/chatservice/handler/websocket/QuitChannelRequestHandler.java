package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.QuitChannelRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.QuitChannelResponse;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class QuitChannelRequestHandler implements BaseRequestHandler<QuitChannelRequest> {


    private static final Logger log = LoggerFactory.getLogger(QuitChannelRequestHandler.class);

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;


    @Override
    public void handle(WebSocketSession senderSession, QuitChannelRequest request) {
        UserId quitUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        ResultType result;

        try {
            result = channelService.quitChannel(quitUserId, request.getChannelId());
        } catch (Exception ex) {
            clientNotificationService.sendMessage(senderSession, quitUserId, new ErrorResponse(MessageType.QUIT_CHANNEL_REQUEST, ResultType.FAILED.getMessage()));
            return;
        }

        if (result == ResultType.SUCCESS) {
            clientNotificationService.sendMessage(senderSession, quitUserId, new QuitChannelResponse(request.getChannelId()));
        } else {
            clientNotificationService.sendMessage(senderSession, quitUserId, new ErrorResponse(MessageType.QUIT_CHANNEL_REQUEST, result.getMessage()));
        }
    }
}
