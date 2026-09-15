package com.iseekfree.common.sdk.grpc.server.autoconfigure;

import com.iseekfree.common.sdk.grpc.server.GrpcServerLifecycle;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Collection;
import java.util.List;

@AutoConfiguration
@ConditionalOnClass(BindableService.class)
@ConditionalOnProperty(prefix = "framework.grpc.server", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AtlasGrpcServerProperties.class)
public class AtlasGrpcServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GrpcServerLifecycle grpcServerLifecycle(AtlasGrpcServerProperties properties,
                                                   Collection<BindableService> bindableServices,
                                                   List<ServerInterceptor> interceptors,
                                                   ApplicationContext applicationContext) {
        return new GrpcServerLifecycle(properties, bindableServices, interceptors, applicationContext);
    }
}
