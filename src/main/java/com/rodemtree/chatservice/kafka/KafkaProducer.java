package com.rodemtree.chatservice.kafka;

import com.rodemtree.chatservice.dto.domain.ChannelId;
import com.rodemtree.chatservice.dto.domain.UserId;
import com.rodemtree.chatservice.dto.kafka.RecordInterface;
import com.rodemtree.chatservice.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    private final JsonUtil jsonUtil;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String pushTopic;

    public KafkaProducer(
            JsonUtil jsonUtil,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${message-system.kafka.topics.push}") String pushTopic
    ) {
        this.jsonUtil = jsonUtil;
        this.kafkaTemplate = kafkaTemplate;
        this.pushTopic = pushTopic;
    }

    public void sendResponse(String topic, RecordInterface recordInterface) {
        jsonUtil.toJson(recordInterface).ifPresent(json ->
                kafkaTemplate.send(topic, json).whenComplete(logResult(topic, json, null))
        );
    }

    public void sendMessageUsingPartitionKey(String topic, ChannelId channelId, UserId userId, RecordInterface recordInterface) {
        String partitionKey = "%d-%d".formatted(channelId.id(), userId.id());
        jsonUtil.toJson(recordInterface).ifPresent(json ->
                kafkaTemplate.send(topic, partitionKey, json).whenComplete(logResult(topic, json, partitionKey))
        );
    }

    public void sendPushNotification(RecordInterface recordInterface) {
        jsonUtil.toJson(recordInterface).ifPresent(json ->
                kafkaTemplate.send(pushTopic, json).whenComplete(logResult(pushTopic, json, null))
        );
    }

    @NonNull
    private BiConsumer<SendResult<String, String>, Throwable> logResult(String topic, String record, String partitionKey) {
        return (result, ex) -> {
            if (ex == null) {
                log.info("Record produced: {} with key: {} to topic: {}",
                        result.getProducerRecord().value(),
                        partitionKey,
                        result.getProducerRecord().topic()
                );
            } else {
                log.error("Record producing failed: {} with key: {} to topic: {}, cause: {}",
                        record,
                        partitionKey,
                        topic,
                        ex.getMessage()
                );
            }
        };
    }
}
