package com.iseekfree.common.sdk.mongo;

import dev.morphia.Datastore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ClawMongoAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ClawMongoAutoConfiguration.class));

    @Test
    void createsMultipleClustersAndDatabasesWithoutAnImplicitDefaultCluster() {
        contextRunner.withPropertyValues(
                "claw.mongo.auto-index=false",
                "claw.mongo.clusters.primary.uri=mongodb://127.0.0.1:27017",
                "claw.mongo.clusters.primary.datastores.catalog.database=catalog",
                "claw.mongo.clusters.primary.datastores.audit.database=audit",
                "claw.mongo.clusters.secondary.uri=mongodb://127.0.0.1:27018",
                "claw.mongo.clusters.secondary.datastores.sales.database=sales",
                "claw.mongo.clusters.secondary.datastores.archive.database=archive"
        ).run(context -> {
            assertThat(context).hasNotFailed();

            MongoClusterRegistry clusters = context.getBean(MongoClusterRegistry.class);
            assertThat(clusters.getClients()).containsOnlyKeys("primary", "secondary");

            MorphiaDatastoreRegistry datastores = context.getBean(MorphiaDatastoreRegistry.class);
            assertThat(datastores.getDatastores()).containsOnlyKeys("catalog", "audit", "sales", "archive");
            assertThat(datastores.getClusterName("audit")).contains("primary");
            assertThat(datastores.getClusterName("archive")).contains("secondary");

            assertThat(context.getBean("catalog", Datastore.class))
                    .isSameAs(datastores.getDatastore("catalog").orElseThrow());
            assertThat(context.getBean("audit", Datastore.class))
                    .isSameAs(datastores.getDatastore("audit").orElseThrow());
            assertThat(context.getBeansOfType(Datastore.class)).containsOnlyKeys("catalog", "audit", "sales", "archive");
        });
    }

    @Test
    void failsAtStartupWhenDatastoreNamesAreDuplicatedAcrossClusters() {
        contextRunner.withPropertyValues(
                "claw.mongo.clusters.primary.uri=mongodb://127.0.0.1:27017",
                "claw.mongo.clusters.primary.datastores.shared.database=catalog",
                "claw.mongo.clusters.secondary.uri=mongodb://127.0.0.1:27018",
                "claw.mongo.clusters.secondary.datastores.shared.database=sales"
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("Duplicate Mongo datastore name: shared");
        });
    }

    @Test
    void keepsLegacyTopLevelConfigurationAsTheDefaultDatastore() {
        contextRunner.withPropertyValues(
                "claw.mongo.auto-index=false",
                "claw.mongo.database=legacy"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            MorphiaDatastoreRegistry datastores = context.getBean(MorphiaDatastoreRegistry.class);
            assertThat(datastores.getDatastores()).containsOnlyKeys("default");
            assertThat(datastores.getDatastore("default").orElseThrow().getDatabase().getName()).isEqualTo("legacy");
        });
    }

    @Test
    void autoIndexOverridesInheritFromTheirParent() {
        assertThat(ClawMongoAutoConfiguration.resolveAutoIndex(null, true)).isTrue();
        assertThat(ClawMongoAutoConfiguration.resolveAutoIndex(null, false)).isFalse();
        assertThat(ClawMongoAutoConfiguration.resolveAutoIndex(false, true)).isFalse();
        assertThat(ClawMongoAutoConfiguration.resolveAutoIndex(true, false)).isTrue();
    }

    @Test
    void initializesIndexesOnlyWhenEnabledForTheDatastore() {
        AtomicInteger enabledCalls = new AtomicInteger();
        AtomicInteger disabledCalls = new AtomicInteger();
        Datastore enabled = countingDatastore(enabledCalls);
        Datastore disabled = countingDatastore(disabledCalls);

        ClawMongoAutoConfiguration.initializeIndexes(enabled, true);
        ClawMongoAutoConfiguration.initializeIndexes(disabled, false);

        assertThat(enabledCalls).hasValue(1);
        assertThat(disabledCalls).hasValue(0);
    }

    private Datastore countingDatastore(AtomicInteger ensureIndexesCalls) {
        return (Datastore) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Datastore.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("ensureIndexes")) {
                        ensureIndexesCalls.incrementAndGet();
                    }
                    return null;
                }
        );
    }
}
