package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.LeaveChannelRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.LeaveChannelResponse;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class LeaveChannelRequestHandler implements BaseRequestHandler<LeaveChannelRequest> {


    private static final Logger log = LoggerFactory.getLogger(LeaveChannelRequestHandler.class);

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;


    @Override
    public void handle(WebSocketSession senderSession, LeaveChannelRequest request) {
        UserId leaveUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        if (channelService.leaveChannel(leaveUserId)) {
            clientNotificationService.sendMessage(senderSession, leaveUserId, new LeaveChannelResponse());
        } else {
            clientNotificationService.sendMessage(senderSession, leaveUserId, new ErrorResponse(MessageType.LEAVE_CHANNEL_REQUEST, "Failed to leave channel"));
        }
    }
}
