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
        "claw.grpc.server.enabled=false",
        "claw.grpc.client.enabled=false",
        "claw.mongo.enabled=true",
        "claw.mongo.clusters.demo.uri=mongodb://root:iseekliqNoegkpsZrnqlchwE72we073mc@115.29.220.3:16673/admin?replicaSet=rs0&directConnection=true",
        "claw.mongo.clusters.demo.auto-index=false",
        "claw.mongo.clusters.demo.datastores.demo.database=claw-sdk-demo",
        "claw.mongo.clusters.demo.datastores.demo.map-packages[0]=com.iseekfree.common.sdk.demo",
        "claw.redis.enabled=true",
        "claw.redis.host=r-bp1qbhon70753useripd.redis.rds.aliyuncs.com",
        "claw.redis.port=6379",
        "claw.redis.database=1",
        "claw.redis.password=qYYeNBu6xpsZrnDuLuwEAcvde",
        "claw.redis.timeout=8s",
        "claw.redis.pool.max-active=1000",
        "claw.redis.pool.max-idle=2",
        "claw.redis.pool.min-idle=100",
        "claw.redis.pool.max-wait=8s"
})
class DemoDataIntegrationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MorphiaDatastoreRegistry datastoreRegistry;

    @Test
    void redisBasicOperationsWork() {
        String key = "claw-sdk-demo:test:" + UUID.randomUUID();
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
