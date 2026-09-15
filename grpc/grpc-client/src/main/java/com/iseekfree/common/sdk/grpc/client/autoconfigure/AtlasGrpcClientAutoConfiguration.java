package com.iseekfree.common.sdk.grpc.client.autoconfigure;

import com.iseekfree.common.sdk.grpc.client.GrpcChannelFactory;
import com.iseekfree.common.sdk.grpc.client.inject.GrpcClientBeanPostProcessor;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass(ManagedChannel.class)
@ConditionalOnProperty(prefix = "framework.grpc.client", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AtlasGrpcClientProperties.class)
public class AtlasGrpcClientAutoConfiguration {

    /**
     * Creates the channel factory together with every business-supplied {@link ClientInterceptor}.
     *
     * <p>The SDK deliberately installs no auth interceptor: which HTTP header carries the caller
     * token and which gRPC metadata key the callee reads are business contracts. Declare
     * {@code ClientInterceptor} beans instead; they are ordered with
     * {@link org.springframework.core.annotation.AnnotationAwareOrderComparator} and mounted on
     * every channel.</p>
     */
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
