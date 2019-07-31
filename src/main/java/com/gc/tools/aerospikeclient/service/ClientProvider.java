package com.gc.tools.aerospikeclient.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PreDestroy;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.policy.ClientPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.gc.tools.aerospikeclient.entities.Connection;
import com.gc.tools.aerospikeclient.repository.ConnectionRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClientProvider {
    private final Map<Long, AerospikeClient> clients = new HashMap<>();
    private final Lock createLock = new ReentrantLock();
    private final ConnectionRepository connectionRepository;

    @PreDestroy
    void destroy() {
        clients.forEach((connectionId, client) -> {
            try {
                log.info("Close client client for connection id {}", connectionId);
                client.close();
            } catch (Exception ex) {
                log.error("Could not close client for connection id {}", connectionId, ex);
            }
        });
    }

    public AerospikeClient provide(Long connectionId) {
        if (!clients.containsKey(connectionId)) {
            createLock.lock();
            try {
                if (!clients.containsKey(connectionId)) {
                    Connection connection = getConnection(connectionId);
                    AerospikeClient client = connect(connection);
                    clients.put(connectionId, client);
                }
            } finally {
                createLock.unlock();
            }
        }
        return clients.get(connectionId);
    }

    private Connection getConnection(Long connectionId) {
        return connectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("No connection found " + connectionId));
    }

    private AerospikeClient connect(Connection connection) {
        ClientPolicy policy = new ClientPolicy();
        policy.user = connection.getUsername();
        policy.password = connection.getPassword();
        Host[] hosts = Host.parseHosts(connection.getHost(), connection.getPort());
        AerospikeClient aerospikeClient = new AerospikeClient(policy, hosts);
        log.info("Aerospike client is connected {}", aerospikeClient.isConnected());
        return aerospikeClient;
    }

}
