package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.mongo.MorphiaDatastoreRegistry;
import dev.morphia.query.filters.Filters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "framework.grpc.server.enabled=false",
        "framework.grpc.client.enabled=false",
        "framework.mongo.enabled=true",
        "framework.mongo.clusters.demo.uri=mongodb://root:iseekliqNoegkpsZrnqlchwE72we073mc@115.29.220.3:16673/admin?replicaSet=rs0&directConnection=true",
        "framework.mongo.clusters.demo.auto-index=false",
        "framework.mongo.clusters.demo.datastores.demo.database=atlas-sdk-demo",
        "framework.mongo.clusters.demo.datastores.demo.map-packages[0]=com.iseekfree.common.sdk.demo",
        "framework.redis.enabled=true",
        "framework.redis.host=r-bp1qbhon70753useripd.redis.rds.aliyuncs.com",
        "framework.redis.port=6379",
        "framework.redis.database=1",
        "framework.redis.password=qYYeNBu6xpsZrnDuLuwEAcvde",
        "framework.redis.timeout=8s",
        "framework.redis.pool.max-active=1000",
        "framework.redis.pool.max-idle=2",
        "framework.redis.pool.min-idle=100",
        "framework.redis.pool.max-wait=8s"
})
class DemoDataIntegrationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MorphiaDatastoreRegistry datastoreRegistry;

    @Test
    void redisBasicOperationsWork() {
        String key = "atlas-sdk-demo:test:" + UUID.randomUUID();
        try {
            redisTemplate.opsForValue().set(key, "ok", Duration.ofMinutes(1));
            assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("ok");
        } finally {
            redisTemplate.delete(key);
        }
    }

    @Test
    void mongoBasicOperationsWorkOnNamedClusterDatastore() {
        assertThat(datastoreRegistry.getClusterName("demo")).contains("demo");

        DemoThing thing = new DemoThing();
        thing.setName("thing-" + UUID.randomUUID());

        datastoreRegistry.save("demo", thing);
        try {
            assertThat(thing.getId()).isNotNull();
            DemoThing found = datastoreRegistry.find("demo", DemoThing.class)
                    .filter(Filters.eq("_id", thing.getId()))
                    .first();
            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo(thing.getName());
        } finally {
            if (thing.getId() != null) {
                datastoreRegistry.delete("demo", thing);
            }
        }
    }
}
