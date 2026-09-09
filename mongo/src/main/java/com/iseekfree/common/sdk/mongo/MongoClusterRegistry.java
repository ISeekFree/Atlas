package com.iseekfree.common.sdk.mongo;

import com.mongodb.client.MongoClient;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MongoClusterRegistry implements AutoCloseable {

    private final Map<String, MongoClient> clients;
    private final Set<String> ownedClientNames;

    public MongoClusterRegistry(Map<String, MongoClient> clients, Set<String> ownedClientNames) {
        this.clients = Collections.unmodifiableMap(new LinkedHashMap<>(clients));
        this.ownedClientNames = Set.copyOf(ownedClientNames);
    }

    public Map<String, MongoClient> getClients() {
        return clients;
    }

    public Optional<MongoClient> getClient(String cluster) {
        return Optional.ofNullable(clients.get(cluster));
    }

    public MongoClient requireClient(String cluster) {
        MongoClient client = clients.get(cluster);
        if (client == null) {
            throw new IllegalArgumentException("Unknown Mongo cluster: " + cluster);
        }
        return client;
    }

    @Override
    public void close() {
        for (String name : ownedClientNames) {
            MongoClient client = clients.get(name);
            if (client != null) {
                client.close();
            }
        }
    }
}
