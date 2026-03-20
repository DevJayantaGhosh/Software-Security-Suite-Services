package com.jayanta.projectmanagement.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Slf4j
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${mongodb.is-cloud-db:false}")
    private boolean isCloudDb;

    @Value("${mongodb.uri:}")
    private String uri;

    @Value("${mongodb.host:localhost}")
    private String host;

    @Value("${mongodb.port:27017}")
    private int port;

    @Value("${mongodb.database:product_management_db}")
    private String database;

    @Value("${mongodb.username:}")
    private String username;

    @Value("${mongodb.password:}")
    private String password;

    @Value("${mongodb.authentication-database:admin}")
    private String authenticationDatabase;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        if (isCloudDb) {
            log.info("Connecting to MongoDB using URI (cloud mode)");
            ConnectionString connectionString = new ConnectionString(uri);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            return MongoClients.create(settings);
        } else {
            log.info("Connecting to MongoDB at {}:{}/{}", host, port, database);
            MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                    .applyToClusterSettings(builder ->
                            builder.hosts(Collections.singletonList(new ServerAddress(host, port))));

            if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
                MongoCredential credential = MongoCredential.createCredential(
                        username, authenticationDatabase, password.toCharArray());
                settingsBuilder.credential(credential);
                log.info("MongoDB authentication enabled for user: {}", username);
            } else {
                log.info("MongoDB connecting without authentication");
            }

            return MongoClients.create(settingsBuilder.build());
        }
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}