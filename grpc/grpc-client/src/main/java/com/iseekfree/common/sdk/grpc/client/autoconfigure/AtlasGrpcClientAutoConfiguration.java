package com.iseekfree.common.sdk.grpc.client.autoconfigure;

import com.iseekfree.common.sdk.grpc.client.GrpcChannelFactory;
import com.iseekfree.common.sdk.grpc.client.GrpcClientAuthInterceptor;
import com.iseekfree.common.sdk.grpc.client.inject.GrpcClientBeanPostProcessor;
import com.iseekfree.common.sdk.web.autoconfigure.AtlasWebProperties;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass(ManagedChannel.class)
@ConditionalOnProperty(prefix = "framework.grpc.client", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AtlasGrpcClientProperties.class)
public class AtlasGrpcClientAutoConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 20)
    @ConditionalOnMissingBean
    public ClientInterceptor grpcClientAuthInterceptor(ObjectProvider<AtlasWebProperties> webProperties) {
        return new GrpcClientAuthInterceptor(webProperties.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcChannelFactory grpcChannelFactory(AtlasGrpcClientProperties properties, List<ClientInterceptor> interceptors) {
        return new GrpcChannelFactory(properties, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public static GrpcClientBeanPostProcessor grpcClientBeanPostProcessor(ObjectProvider<GrpcChannelFactory> channelFactory) {
        return new GrpcClientBeanPostProcessor(channelFactory);
    }
}
