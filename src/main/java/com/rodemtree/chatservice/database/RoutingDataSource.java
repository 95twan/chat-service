package com.rodemtree.chatservice.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class RoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(RoutingDataSource.class);

    @Nullable
    @Override
    protected Object determineCurrentLookupKey() {
        String dataSourceKey = TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "replica" : "source";
        log.info("Routing to {} DataSource.", dataSourceKey);
        return dataSourceKey;
    }
}
