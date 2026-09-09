package com.iseekfree.common.sdk.grpc.server.autoconfigure;

import com.iseekfree.common.sdk.common.auth.AuthService;
import com.iseekfree.common.sdk.grpc.server.GrpcServerLifecycle;
import com.iseekfree.common.sdk.grpc.server.auth.GrpcAuthServerInterceptor;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Collection;
import java.util.List;

@AutoConfiguration
@ConditionalOnClass(BindableService.class)
@ConditionalOnProperty(prefix = "claw.grpc.server", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClawGrpcServerProperties.class)
public class ClawGrpcServerAutoConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 20)
    @ConditionalOnProperty(prefix = "claw.grpc.server.auth", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ServerInterceptor grpcAuthServerInterceptor(ClawGrpcServerProperties properties, AuthService authService) {
        return new GrpcAuthServerInterceptor(properties, authService);
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcServerLifecycle grpcServerLifecycle(ClawGrpcServerProperties properties, Collection<BindableService> bindableServices, List<ServerInterceptor> interceptors) {
        return new GrpcServerLifecycle(properties, bindableServices, interceptors);
    }
}
