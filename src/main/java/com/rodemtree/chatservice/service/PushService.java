package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.outbound.RecordInterface;
import com.rodemtree.chatservice.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class PushService {

    private static final Logger log = LoggerFactory.getLogger(PushService.class);

    private final KafkaProducerService kafkaProducerService;
    private final JsonUtil jsonUtil;
    private final HashMap<String, Class<? extends RecordInterface>> pushMessageTypes = new HashMap<>();

    public void registerPushMessageType(String messageType, Class<? extends RecordInterface> clazz) {
        pushMessageTypes.put(messageType, clazz);
    }

    public void pushMessage(UserId userId, String messageType, String message) {
        Class<? extends RecordInterface> recordInterface = pushMessageTypes.get(messageType);
        if (recordInterface != null) {
            jsonUtil.addValue(message, "userId", userId.id().toString())
                    .flatMap(json -> jsonUtil.fromJson(json, recordInterface))
                    .ifPresent(kafkaProducerService::sendPushNotification);
            log.info("Push message: [{}] to user: {}", message, userId);
        } else {
            log.error("Invalid message type: {}", messageType);
        }

    }
}
