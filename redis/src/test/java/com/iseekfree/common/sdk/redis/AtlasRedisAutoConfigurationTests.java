package com.iseekfree.common.sdk.redis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AtlasRedisAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AtlasRedisAutoConfiguration.class));

    @Test
    void bindsAllConnectionSettingsFromAtlasRedisProperties() {
        contextRunner.withPropertyValues(
                "framework.redis.host=redis.internal",
                "framework.redis.port=6380",
                "framework.redis.database=3",
                "framework.redis.username=app",
                "framework.redis.password=secret",
                "framework.redis.client-name=atlas-sdk-test",
                "framework.redis.connect-timeout=2s",
                "framework.redis.timeout=4s",
                "framework.redis.shutdown-timeout=200ms",
                "framework.redis.pool.max-active=32",
                "framework.redis.pool.max-idle=12",
                "framework.redis.pool.min-idle=2",
                "framework.redis.pool.max-wait=3s",
                "framework.redis.pool.time-between-eviction-runs=30s"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasBean("redisConnectionFactory");
            assertThat(context).hasBean("redisTemplateWithObject");
            assertThat(context).hasBean("redisTemplate");
            assertThat(context).hasBean("redisKey");

            AtlasRedisProperties properties = context.getBean(AtlasRedisProperties.class);
            assertThat(properties.getHost()).isEqualTo("redis.internal");
            assertThat(properties.getPort()).isEqualTo(6380);
            assertThat(properties.getDatabase()).isEqualTo(3);
            assertThat(properties.getUsername()).isEqualTo("app");
            assertThat(properties.getPassword()).isEqualTo("secret");
            assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(2));
            assertThat(properties.getTimeout()).isEqualTo(Duration.ofSeconds(4));

            LettuceConnectionFactory connectionFactory = context.getBean(LettuceConnectionFactory.class);
            assertThat(connectionFactory.getHostName()).isEqualTo("redis.internal");
            assertThat(connectionFactory.getPort()).isEqualTo(6380);
            assertThat(connectionFactory.getDatabase()).isEqualTo(3);
            assertThat(connectionFactory.getClientConfiguration().getCommandTimeout()).isEqualTo(Duration.ofSeconds(4));
            assertThat(connectionFactory.getClientConfiguration()).isInstanceOf(LettucePoolingClientConfiguration.class);

            LettucePoolingClientConfiguration clientConfiguration =
                    (LettucePoolingClientConfiguration) connectionFactory.getClientConfiguration();
            assertThat(clientConfiguration.getPoolConfig().getMaxTotal()).isEqualTo(32);
            assertThat(clientConfiguration.getPoolConfig().getMaxIdle()).isEqualTo(12);
            assertThat(clientConfiguration.getPoolConfig().getMinIdle()).isEqualTo(2);
            assertThat(clientConfiguration.getPoolConfig().getMaxWaitMillis()).isEqualTo(3000);
        });
    }

    @Test
    void createsNoRedisInfrastructureWhenAtlasRedisIsDisabled() {
        contextRunner.withPropertyValues(
                "framework.redis.enabled=false",
                "spring.data.redis.host=legacy-config-must-not-be-used"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(RedisConnectionFactory.class);
            assertThat(context).doesNotHaveBean(RedisTemplate.class);
            assertThat(context).doesNotHaveBean(AtlasRedisKey.class);
        });
    }
}
