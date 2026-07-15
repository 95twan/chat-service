package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.dto.kafka.RecordInterface;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@SuppressWarnings({"rawtypes", "unchecked"})
@RequiredArgsConstructor
public class RecordDispatcher {

    private static final Logger log = LoggerFactory.getLogger(RecordDispatcher.class);

    private final Map<Class<? extends RecordInterface>, BaseRecordHandler<? extends RecordInterface>> handlerMap = new HashMap<>();
    private final ListableBeanFactory listableBeanFactory;

    public <T extends RecordInterface> void dispatchRecord(T record) {
        BaseRecordHandler<T> handler = (BaseRecordHandler<T>) handlerMap.get(record.getClass());
        if (handler != null) {
            handler.handleRecord(record);
            return;
        }
        log.error("Handler not found for record type: {}", record.getClass().getSimpleName());
    }

    @PostConstruct
    private void prepareRecordHandlerMapping() {
        Map<String, BaseRecordHandler> beanHandlers = listableBeanFactory.getBeansOfType(BaseRecordHandler.class);
        for (BaseRecordHandler handler : beanHandlers.values()) {
            handlerMap.put(handler.recordType(), handler);
        }
    }
}
