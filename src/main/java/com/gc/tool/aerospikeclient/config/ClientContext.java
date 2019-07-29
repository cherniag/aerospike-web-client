package com.gc.tool.aerospikeclient.config;

import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientContext {

    @Bean
    public WritePolicy defaultUpdatePolicy(@Value("${client.policy.update.socketTimeout}") int socketTimeout,
                                           @Value("${client.policy.update.totalTimeout}") int totalTimeout,
                                           @Value("${client.policy.update.sleepBetweenRetries}") int sleepBetweenRetries,
                                           @Value("${client.policy.update.maxRetries}") int maxRetries) {
        WritePolicy writePolicy = new WritePolicy();
        writePolicy.socketTimeout = socketTimeout;
        writePolicy.totalTimeout = totalTimeout;
        writePolicy.sleepBetweenRetries = sleepBetweenRetries;
        writePolicy.maxRetries = maxRetries;
        return writePolicy;
    }

    @Bean
    public WritePolicy defaultCreatePolicy(@Value("${client.policy.create.socketTimeout}") int socketTimeout,
                                           @Value("${client.policy.create.totalTimeout}") int totalTimeout,
                                           @Value("${client.policy.create.sleepBetweenRetries}") int sleepBetweenRetries,
                                           @Value("${client.policy.create.maxRetries}") int maxRetries) {
        WritePolicy writePolicy = new WritePolicy();
        writePolicy.socketTimeout = socketTimeout;
        writePolicy.totalTimeout = totalTimeout;
        writePolicy.sleepBetweenRetries = sleepBetweenRetries;
        writePolicy.maxRetries = maxRetries;
        return writePolicy;
    }

    @Bean
    public WritePolicy defaultDeletePolicy(@Value("${client.policy.delete.socketTimeout}") int socketTimeout,
                                           @Value("${client.policy.delete.totalTimeout}") int totalTimeout,
                                           @Value("${client.policy.delete.sleepBetweenRetries}") int sleepBetweenRetries,
                                           @Value("${client.policy.delete.maxRetries}") int maxRetries) {
        WritePolicy writePolicy = new WritePolicy();
        writePolicy.socketTimeout = socketTimeout;
        writePolicy.totalTimeout = totalTimeout;
        writePolicy.sleepBetweenRetries = sleepBetweenRetries;
        writePolicy.maxRetries = maxRetries;
        return writePolicy;
    }

    @Bean
    public QueryPolicy defaultReadPolicy(@Value("${client.policy.read.socketTimeout}") int socketTimeout,
                                         @Value("${client.policy.read.totalTimeout}") int totalTimeout,
                                         @Value("${client.policy.read.sleepBetweenRetries}") int sleepBetweenRetries,
                                         @Value("${client.policy.read.maxRetries}") int maxRetries) {
        QueryPolicy queryPolicy = new QueryPolicy();
        queryPolicy.socketTimeout = socketTimeout;
        queryPolicy.totalTimeout = totalTimeout;
        queryPolicy.sleepBetweenRetries = sleepBetweenRetries;
        queryPolicy.maxRetries = maxRetries;
        return queryPolicy;
    }

    @Bean
    public ObjectMapper clientObjectMapper() {
        return new ObjectMapper();
    }
}
