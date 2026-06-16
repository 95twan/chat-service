package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.EnterChannelRequest;
import com.rodemtree.chatservice.dto.websocket.inbound.LeaveChannelRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.EnterChannelResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.LeaveChannelResponse;
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
public class LeaveChannelRequestHandler implements BaseRequestHandler<LeaveChannelRequest> {


    private static final Logger log = LoggerFactory.getLogger(LeaveChannelRequestHandler.class);

    private final ChannelService channelService;
    private final WebSocketSessionManager webSocketSessionManager;


    @Override
    public void handle(WebSocketSession senderSession, LeaveChannelRequest request) {
        UserId leaveUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        if (channelService.leaveChannel(leaveUserId)) {
            webSocketSessionManager.sendMessage(senderSession, new LeaveChannelResponse());
        } else {
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.LEAVE_CHANNEL_REQUEST, "Failed to leave channel"));
        }
    }
}
