package com.iseekfree.common.sdk.grpc.server.autoconfigure;

import com.iseekfree.common.sdk.grpc.server.GrpcStatusExceptionResponseResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Registers the gRPC-to-HTTP exception bridge whenever both the WebMVC SDK and
 * a gRPC runtime are present, independent of whether this service also exposes
 * a gRPC server ({@code framework.grpc.server.enabled}); an HTTP-only service
 * that merely calls downstream stubs still gets the mapping.
 */
@AutoConfiguration
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnProperty(prefix = "framework.web", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AtlasGrpcWebExceptionAutoConfiguration {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    @ConditionalOnMissingBean(GrpcStatusExceptionResponseResolver.class)
    public GrpcStatusExceptionResponseResolver grpcStatusExceptionResponseResolver() {
        return new GrpcStatusExceptionResponseResolver();
    }
}
