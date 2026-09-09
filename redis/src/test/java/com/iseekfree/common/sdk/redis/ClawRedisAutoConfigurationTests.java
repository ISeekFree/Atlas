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

class ClawRedisAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ClawRedisAutoConfiguration.class));

    @Test
    void bindsAllConnectionSettingsFromClawRedisProperties() {
        contextRunner.withPropertyValues(
                "claw.redis.host=redis.internal",
                "claw.redis.port=6380",
                "claw.redis.database=3",
                "claw.redis.username=app",
                "claw.redis.password=secret",
                "claw.redis.client-name=claw-sdk-test",
                "claw.redis.connect-timeout=2s",
                "claw.redis.timeout=4s",
                "claw.redis.shutdown-timeout=200ms",
                "claw.redis.pool.max-active=32",
                "claw.redis.pool.max-idle=12",
                "claw.redis.pool.min-idle=2",
                "claw.redis.pool.max-wait=3s",
                "claw.redis.pool.time-between-eviction-runs=30s"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasBean("redisConnectionFactory");
            assertThat(context).hasBean("redisTemplateWithObject");
            assertThat(context).hasBean("redisTemplate");
            assertThat(context).hasBean("redisKey");

            ClawRedisProperties properties = context.getBean(ClawRedisProperties.class);
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
    void createsNoRedisInfrastructureWhenClawRedisIsDisabled() {
        contextRunner.withPropertyValues(
                "claw.redis.enabled=false",
                "spring.data.redis.host=legacy-config-must-not-be-used"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(RedisConnectionFactory.class);
            assertThat(context).doesNotHaveBean(RedisTemplate.class);
            assertThat(context).doesNotHaveBean(ClawRedisKey.class);
        });
    }
}
