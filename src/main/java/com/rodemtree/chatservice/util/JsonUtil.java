package com.rodemtree.chatservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private final ObjectMapper objectMapper;


    public <T> Optional<T> fromJson(String json, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (Exception ex) {
            log.error("Failed JSON to Object: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        try {
            return objectMapper.readerForListOf(clazz).readValue(json);
        } catch (Exception ex) {
            log.error("Failed JSON to List: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<String> toJson(Object object) {
        try {
            return Optional.of(objectMapper.writeValueAsString(object));
        } catch (Exception e) {
            log.error("Failed Object to JSON: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> addValue(String json, String key, String value) {
        try {
            ObjectNode node = (ObjectNode) objectMapper.readTree(json);
            node.put(key, value);
            return Optional.of(objectMapper.writeValueAsString(node));
        } catch (Exception ex) {
            log.error("Failed adding value to JSON: {}, cause: {}", json, ex.getMessage());
            return Optional.empty();
        }
    }
}
