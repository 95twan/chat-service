package com.rodemtree.chatservice.handler.websocket;

import com.rodemtree.chatservice.constant.IdKey;
import com.rodemtree.chatservice.constant.MessageType;
import com.rodemtree.chatservice.constant.ResultType;
import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.Message;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.websocket.inbound.FetchMessagesRequest;
import com.rodemtree.chatservice.dto.websocket.outbound.ErrorResponse;
import com.rodemtree.chatservice.dto.websocket.outbound.FetchMessagesResponse;
import com.rodemtree.chatservice.service.ClientNotificationService;
import com.rodemtree.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FetchMessagesRequestHandler implements BaseRequestHandler<FetchMessagesRequest> {

    private final MessageService messageService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handle(WebSocketSession senderSession, FetchMessagesRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        ChannelId channelId = request.getChannelId();
        Pair<List<Message>, ResultType> result = messageService.getMessages(channelId, request.getStartMessageSeqId(), request.getEndMessageSeqId());
        if (result.getSecond() == ResultType.SUCCESS) {
            List<Message> messages = result.getFirst();
            clientNotificationService.sendMessage(senderSession, senderUserId, new FetchMessagesResponse(channelId, messages));
        } else {
            clientNotificationService.sendMessage(senderSession, senderUserId, new ErrorResponse(MessageType.FETCH_MESSAGES_REQUEST, result.getSecond().getMessage()));
        }
    }
}
