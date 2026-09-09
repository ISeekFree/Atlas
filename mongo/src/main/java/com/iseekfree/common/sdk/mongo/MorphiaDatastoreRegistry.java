package com.iseekfree.common.sdk.mongo;

import com.mongodb.client.result.DeleteResult;
import dev.morphia.Datastore;
import dev.morphia.InsertManyOptions;
import dev.morphia.InsertOneOptions;
import dev.morphia.aggregation.Aggregation;
import dev.morphia.mapping.codec.pojo.EntityModel;
import dev.morphia.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MorphiaDatastoreRegistry {

    private final Map<String, Datastore> datastores;
    private final Map<String, String> datastoreClusters;
    private final Map<Class<?>, List<String>> entityDatastoreNames = new LinkedHashMap<>();

    public MorphiaDatastoreRegistry(Map<String, Datastore> datastores, Map<String, String> datastoreClusters) {
        this.datastores = Collections.unmodifiableMap(new LinkedHashMap<>(datastores));
        this.datastoreClusters = Collections.unmodifiableMap(new LinkedHashMap<>(datastoreClusters));
        collectEntityMappings();
    }

    public Map<String, Datastore> getDatastores() {
        return datastores;
    }

    public Optional<Datastore> getDatastore(String name) {
        return Optional.ofNullable(datastores.get(name));
    }

    public Optional<String> getClusterName(String datastoreName) {
        return Optional.ofNullable(datastoreClusters.get(datastoreName));
    }

    public Datastore getDatastore(Class<?> entityType) {
        List<String> names = entityDatastoreNames.get(entityType);
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("Class " + entityType.getName() + " is not mapped by Morphia");
        }
        if (names.size() > 1) {
            throw new IllegalArgumentException("Class " + entityType.getName()
                    + " is mapped by multiple Mongo datastores " + names
                    + "; use getDatastore(name), find(name, type), or save(name, entity)");
        }
        return datastores.get(names.get(0));
    }

    public <T> Query<T> find(Class<T> entityType) {
        return getDatastore(entityType).find(entityType);
    }

    public <T> Query<T> find(String datastoreName, Class<T> entityType) {
        return requireDatastore(datastoreName).find(entityType);
    }

    public <T> T save(T entity) {
        return getDatastore(entity.getClass()).save(entity);
    }

    public <T> T save(String datastoreName, T entity) {
        return requireDatastore(datastoreName).save(entity);
    }

    public <T> T save(T entity, InsertOneOptions options) {
        return getDatastore(entity.getClass()).save(entity, options);
    }

    public <T> T save(String datastoreName, T entity, InsertOneOptions options) {
        return requireDatastore(datastoreName).save(entity, options);
    }

    public <T> List<T> saveAll(Iterable<T> entities) {
        List<T> saved = new ArrayList<>();
        for (T entity : entities) {
            saved.add(save(entity));
        }
        return saved;
    }

    public <T> List<T> saveAll(String datastoreName, Iterable<T> entities) {
        List<T> saved = new ArrayList<>();
        for (T entity : entities) {
            saved.add(save(datastoreName, entity));
        }
        return saved;
    }

    public <T> Iterable<T> insert(Iterable<T> entities, InsertManyOptions options) {
        List<T> list = new ArrayList<>();
        entities.forEach(list::add);
        if (list.isEmpty()) {
            return list;
        }
        getDatastore(list.get(0).getClass()).insert(list, options);
        return list;
    }

    public <T> DeleteResult delete(T entity) {
        return getDatastore(entity.getClass()).delete(entity);
    }

    public <T> DeleteResult delete(String datastoreName, T entity) {
        return requireDatastore(datastoreName).delete(entity);
    }

    public <T> Aggregation<T> aggregate(Class<T> entityType) {
        return getDatastore(entityType).aggregate(entityType);
    }

    public <T> Aggregation<T> aggregate(String datastoreName, Class<T> entityType) {
        return requireDatastore(datastoreName).aggregate(entityType);
    }

    public void collectEntityMappings() {
        entityDatastoreNames.clear();
        datastores.forEach((name, datastore) -> {
            for (EntityModel entityModel : datastore.getMapper().getMappedEntities()) {
                entityDatastoreNames.computeIfAbsent(entityModel.getType(), ignored -> new ArrayList<>()).add(name);
            }
        });
    }

    private Datastore requireDatastore(String name) {
        Datastore datastore = datastores.get(name);
        if (datastore == null) {
            throw new IllegalArgumentException("Unknown Mongo datastore: " + name);
        }
        return datastore;
    }
}
