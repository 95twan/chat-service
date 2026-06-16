package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.dto.domain.Channel;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.FetchChannelsRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.FetchChannelsResponse;
import com.rodemtree.chatservice.service.ChannelService;
import com.rodemtree.chatservice.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FetchChannelsRequestHandler implements BaseRequestHandler<FetchChannelsRequest> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;


    @Override
    public void handle(WebSocketSession senderSession, FetchChannelsRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        List<Channel> channels = channelService.getChannels(senderUserId);

        clientNotificationService.sendMessage(senderSession, senderUserId, new FetchChannelsResponse(channels));
    }
}
