package com.rodemtree.chatservice.service;

import com.rodemtree.chatservice.dto.kafka.RecordInterface;
import com.rodemtree.chatservice.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class PushService {

    private static final Logger log = LoggerFactory.getLogger(PushService.class);

    private final KafkaProducer kafkaProducer;
    private final HashMap<String, Class<? extends RecordInterface>> pushMessageTypes = new HashMap<>();

    public void registerPushMessageType(String messageType, Class<? extends RecordInterface> clazz) {
        pushMessageTypes.put(messageType, clazz);
    }

    public void pushMessage(RecordInterface recordInterface) {
        String messageType = recordInterface.type();
        if (pushMessageTypes.containsKey(messageType)) {
            kafkaProducer.sendPushNotification(recordInterface);
        } else {
            log.error("Invalid message type: {}", messageType);
        }
    }
}
