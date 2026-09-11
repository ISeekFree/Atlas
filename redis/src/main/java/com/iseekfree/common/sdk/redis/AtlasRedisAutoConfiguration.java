package com.iseekfree.common.sdk.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iseekfree.common.sdk.common.json.Jsons;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(prefix = "framework.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AtlasRedisProperties.class)
public class AtlasRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisConnectionFactory redisConnectionFactory(AtlasRedisProperties properties) {
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(properties.getHost(), properties.getPort());
        standalone.setDatabase(properties.getDatabase());
        if (hasText(properties.getUsername())) {
            standalone.setUsername(properties.getUsername());
        }
        if (hasText(properties.getPassword())) {
            standalone.setPassword(RedisPassword.of(properties.getPassword()));
        }
        return new LettuceConnectionFactory(standalone, clientConfiguration(properties));
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper redisObjectMapper() {
        return Jsons.OBJECT_MAPPER;
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnMissingBean(name = "redisTemplateWithObject")
    public RedisTemplate<String, Object> redisTemplateWithObject(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnMissingBean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public AtlasRedisKey redisKey(AtlasRedisProperties properties) {
        return new AtlasRedisKey(properties);
    }

    private LettuceClientConfiguration clientConfiguration(AtlasRedisProperties properties) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder;
        if (properties.getPool().isEnabled()) {
            builder = LettucePoolingClientConfiguration.builder().poolConfig(poolConfig(properties.getPool()));
        } else {
            builder = LettuceClientConfiguration.builder();
        }
        builder.commandTimeout(properties.getTimeout())
                .shutdownTimeout(properties.getShutdownTimeout())
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder().connectTimeout(properties.getConnectTimeout()).build())
                        .build());
        if (hasText(properties.getClientName())) {
            builder.clientName(properties.getClientName());
        }
        if (!properties.getSsl().isEnabled()) {
            return builder.build();
        }
        LettuceClientConfiguration.LettuceSslClientConfigurationBuilder sslBuilder = builder.useSsl();
        if (!properties.getSsl().isVerifyPeer()) {
            sslBuilder.disablePeerVerification();
        }
        if (properties.getSsl().isStartTls()) {
            sslBuilder.startTls();
        }
        return sslBuilder.build();
    }

    private GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig(AtlasRedisProperties.Pool properties) {
        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(properties.getMaxActive());
        poolConfig.setMaxIdle(properties.getMaxIdle());
        poolConfig.setMinIdle(properties.getMinIdle());
        poolConfig.setMaxWaitMillis(properties.getMaxWait().toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRuns().toMillis());
        return poolConfig;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
