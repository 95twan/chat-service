package com.rodemtree.chatservice.handler.kafka;

import com.rodemtree.chatservice.dto.kafka.RecordInterface;

public interface BaseRecordHandler<T extends RecordInterface> {
    Class<T> recordType();

    void handleRecord(T record);
}
