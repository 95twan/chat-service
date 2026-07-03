package com.rodemtree.chatservice.config;

import com.rodemtree.chatservice.database.RoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.source")
    public DataSource sourceDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.source-message1")
    public DataSource sourceMessage1DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica-message1")
    public DataSource replicaMessage1DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.source-message2")
    public DataSource sourceMessage2DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica-message2")
    public DataSource replicaMessage2DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public DataSource routingDataSource(
            @Qualifier("sourceDataSource") DataSource sourceDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource,
            @Qualifier("sourceMessage1DataSource") DataSource sourceMessage1DataSource,
            @Qualifier("replicaMessage1DataSource") DataSource replicaMessage1DataSource,
            @Qualifier("sourceMessage2DataSource") DataSource sourceMessage2DataSource,
            @Qualifier("replicaMessage2DataSource") DataSource replicaMessage2DataSource
    ) throws SQLException {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();

        targetDataSources.put("source", sourceDataSource);
        targetDataSources.put("replica", replicaDataSource);
        targetDataSources.put("sourceMessage1", sourceMessage1DataSource);
        targetDataSources.put("replicaMessage1", replicaMessage1DataSource);
        targetDataSources.put("sourceMessage2", sourceMessage2DataSource);
        targetDataSources.put("replicaMessage2", replicaMessage2DataSource);
        routingDataSource.setTargetDataSources(targetDataSources);

        try(Connection connection = replicaDataSource.getConnection()) {
            log.info("Init ReplicaConnectionPool");
        }

        try(Connection connection = replicaMessage1DataSource.getConnection()) {
            log.info("Init ReplicaConnectionPool");
        }

        try(Connection connection = replicaMessage2DataSource.getConnection()) {
            log.info("Init ReplicaConnectionPool");
        }

        return routingDataSource;
    }

    @Bean
    public DataSourceInitializer sourceDataSourceInitializer(
            @Qualifier("sourceDataSource") DataSource sourceDataSource
    ) {
        return dataSourceInitializer(sourceDataSource, "chatsystem.sql");
    }

    @Bean
    public DataSourceInitializer sourceMessage1DataSourceInitializer(
            @Qualifier("sourceMessage1DataSource") DataSource sourceMessage1DataSource
    ) {
        return dataSourceInitializer(sourceMessage1DataSource, "message.sql");
    }

    @Bean
    public DataSourceInitializer sourceMessage2DataSourceInitializer(
            @Qualifier("sourceMessage2DataSource") DataSource sourceMessage2DataSource
    ) {
        return dataSourceInitializer(sourceMessage2DataSource, "message.sql");
    }

    @Primary
    @Bean
    public DataSource lazyConnectionDataSource(
            @Qualifier("routingDataSource") DataSource routingDataSource
    ) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    private DataSourceInitializer dataSourceInitializer(DataSource dataSource, String script) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource(script));

        initializer.setDatabasePopulator(populator);

        return initializer;
    }
}
