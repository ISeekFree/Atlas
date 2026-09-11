package com.iseekfree.common.sdk.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoConfiguration
@ConditionalOnClass(Morphia.class)
@ConditionalOnProperty(prefix = "framework.mongo", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AtlasMongoProperties.class)
@Import(MongoDatastoreBeanRegistrar.class)
public class AtlasMongoAutoConfiguration {

    public static final String DEFAULT_DATASTORE = "default";
    public static final String DEFAULT_CLUSTER = "default";

    @Bean
    @ConditionalOnMissingBean
    public MongoClusterRegistry mongoClusterRegistry(AtlasMongoProperties properties) {
        Map<String, ClusterDefinition> definitions = clusterDefinitions(properties);
        Map<String, MongoClient> clients = new LinkedHashMap<>();
        Set<String> ownedClientNames = new LinkedHashSet<>();

        definitions.forEach((name, definition) -> {
            clients.put(name, createMongoClient(definition.uri()));
            ownedClientNames.add(name);
        });
        return new MongoClusterRegistry(clients, ownedClientNames);
    }

    @Bean(destroyMethod = "")
    @ConditionalOnMissingBean
    public MongoClient mongoClient(MongoClusterRegistry clusterRegistry) {
        return clusterRegistry.getClient(DEFAULT_CLUSTER)
                .orElseGet(() -> clusterRegistry.getClients().values().iterator().next());
    }

    @Bean
    @ConditionalOnMissingBean
    public MapperOptions mapperOptions() {
        return MapperOptions.builder()
                .storeEmpties(true)
                .storeNulls(false)
                .ignoreFinals(true)
                .mapSubPackages(true)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public MorphiaDatastoreRegistry morphiaDatastoreRegistry(MongoClusterRegistry clusterRegistry, MapperOptions mapperOptions, AtlasMongoProperties properties) {
        Map<String, ClusterDefinition> clusters = clusterDefinitions(properties);
        Map<String, Datastore> datastores = new LinkedHashMap<>();
        Map<String, String> datastoreClusters = new LinkedHashMap<>();

        if (usesLegacyDefaultDatastore(properties)) {
            addDatastore(datastores, datastoreClusters, DEFAULT_DATASTORE, DEFAULT_CLUSTER,
                    clusterRegistry.requireClient(DEFAULT_CLUSTER), properties.getDatabase(), mapperOptions,
                    properties.getMapPackages(), properties.isAutoIndex());
        }

        clusters.forEach((clusterName, cluster) -> {
            cluster.datastores().forEach((name, definition) -> addDatastore(
                    datastores,
                    datastoreClusters,
                    name,
                    clusterName,
                    clusterRegistry.requireClient(clusterName),
                    requireDatabase(name, clusterName, definition.getDatabase()),
                    mapperOptions,
                    definition.getMapPackages(),
                    resolveAutoIndex(definition.getAutoIndex(), cluster.autoIndex())
            ));
        });

        properties.getDatastores().forEach((name, definition) -> {
            String clusterName = firstNonBlank(definition.getCluster(), DEFAULT_CLUSTER);
            ClusterDefinition cluster = clusters.get(clusterName);
            if (cluster == null) {
                throw new IllegalArgumentException("Mongo datastore " + name + " references unknown cluster " + clusterName);
            }
            addDatastore(datastores, datastoreClusters, name, clusterName, clusterRegistry.requireClient(clusterName),
                    firstNonBlank(definition.getDatabase(), properties.getDatabase()), mapperOptions,
                    definition.getMapPackages(),
                    resolveAutoIndex(definition.getAutoIndex(), cluster.autoIndex()));
        });

        return new MorphiaDatastoreRegistry(datastores, datastoreClusters);
    }

    private MongoClient createMongoClient(String uri) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build();
        return MongoClients.create(settings);
    }

    private void addDatastore(Map<String, Datastore> datastores, Map<String, String> datastoreClusters, String datastoreName, String clusterName, MongoClient mongoClient, String database, MapperOptions mapperOptions, List<String> packages, boolean autoIndex) {
        if (datastores.containsKey(datastoreName)) {
            throw new IllegalArgumentException("Duplicate Mongo datastore name: " + datastoreName);
        }
        datastores.put(datastoreName, createDatastore(mongoClient, database, mapperOptions, packages, autoIndex));
        datastoreClusters.put(datastoreName, clusterName);
    }

    private Datastore createDatastore(MongoClient mongoClient, String database, MapperOptions mapperOptions, List<String> packages, boolean autoIndex) {
        Datastore datastore = Morphia.createDatastore(mongoClient, database, mapperOptions);
        for (String packageName : packages) {
            if (packageName != null && !packageName.isBlank()) {
                datastore.getMapper().mapPackage(packageName);
            }
        }
        initializeIndexes(datastore, autoIndex);
        return datastore;
    }

    static void initializeIndexes(Datastore datastore, boolean autoIndex) {
        if (autoIndex) {
            datastore.ensureIndexes();
        }
    }

    static Map<String, ClusterDefinition> clusterDefinitions(AtlasMongoProperties properties) {
        Map<String, ClusterDefinition> definitions = new LinkedHashMap<>();
        if (shouldCreateDefaultCluster(properties)) {
            definitions.put(DEFAULT_CLUSTER, defaultCluster(properties));
        }
        properties.getClusters().forEach((name, cluster) -> {
            if (DEFAULT_CLUSTER.equals(name)) {
                ClusterDefinition base = definitions.getOrDefault(DEFAULT_CLUSTER, defaultCluster(properties));
                definitions.put(DEFAULT_CLUSTER, mergeDefaultCluster(base, cluster));
                return;
            }
            String uri = firstNonBlank(cluster.getUri());
            if (uri == null) {
                throw new IllegalArgumentException("Mongo cluster " + name + " must define uri");
            }
            definitions.put(name, new ClusterDefinition(
                    uri,
                    resolveAutoIndex(cluster.getAutoIndex(), properties.isAutoIndex()),
                    Map.copyOf(cluster.getDatastores())
            ));
        });
        if (definitions.isEmpty()) {
            definitions.put(DEFAULT_CLUSTER, defaultCluster(properties));
        }
        return definitions;
    }

    private static boolean shouldCreateDefaultCluster(AtlasMongoProperties properties) {
        if (properties.getClusters().isEmpty()) {
            return true;
        }
        if (properties.getClusters().containsKey(DEFAULT_CLUSTER)) {
            return true;
        }
        if (!properties.getMapPackages().isEmpty()) {
            return true;
        }
        return properties.getDatastores().values().stream()
                .anyMatch(datastore -> firstNonBlank(datastore.getCluster(), DEFAULT_CLUSTER).equals(DEFAULT_CLUSTER));
    }

    private static ClusterDefinition defaultCluster(AtlasMongoProperties properties) {
        return new ClusterDefinition(
                properties.getUri(),
                properties.isAutoIndex(),
                Map.of()
        );
    }

    private static ClusterDefinition mergeDefaultCluster(ClusterDefinition base, AtlasMongoProperties.Cluster override) {
        return new ClusterDefinition(
                firstNonBlank(override.getUri(), base.uri()),
                resolveAutoIndex(override.getAutoIndex(), base.autoIndex()),
                Map.copyOf(override.getDatastores())
        );
    }

    private static String requireDatabase(String datastoreName, String clusterName, String database) {
        String configuredDatabase = firstNonBlank(database);
        if (configuredDatabase == null) {
            throw new IllegalArgumentException("Mongo datastore " + datastoreName + " in cluster " + clusterName
                    + " must define database");
        }
        return configuredDatabase;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    static boolean resolveAutoIndex(Boolean configured, boolean inherited) {
        return configured == null ? inherited : configured;
    }

    static Set<String> datastoreNames(AtlasMongoProperties properties) {
        Set<String> names = new LinkedHashSet<>();
        if (usesLegacyDefaultDatastore(properties)) {
            addDatastoreName(names, DEFAULT_DATASTORE);
        }
        clusterDefinitions(properties).values().forEach(cluster ->
                cluster.datastores().keySet().forEach(name -> addDatastoreName(names, name)));
        properties.getDatastores().keySet().forEach(name -> addDatastoreName(names, name));
        return names;
    }

    private static boolean usesLegacyDefaultDatastore(AtlasMongoProperties properties) {
        return properties.getClusters().isEmpty() || !properties.getMapPackages().isEmpty();
    }

    private static void addDatastoreName(Set<String> names, String name) {
        if (!names.add(name)) {
            throw new IllegalArgumentException("Duplicate Mongo datastore name: " + name
                    + ". Datastore names must be unique across all Mongo clusters");
        }
    }

    record ClusterDefinition(String uri, boolean autoIndex, Map<String, AtlasMongoProperties.Datastore> datastores) {
    }
}
