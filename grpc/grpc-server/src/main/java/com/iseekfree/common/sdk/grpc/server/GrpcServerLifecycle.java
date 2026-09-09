package com.iseekfree.common.sdk.grpc.server;

import com.iseekfree.common.sdk.grpc.server.autoconfigure.ClawGrpcServerProperties;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GrpcServerLifecycle implements SmartLifecycle {

    private final ClawGrpcServerProperties properties;
    private final Collection<BindableService> bindableServices;
    private final List<ServerInterceptor> interceptors;
    private Server server;
    private boolean running;

    public GrpcServerLifecycle(ClawGrpcServerProperties properties, Collection<BindableService> bindableServices, List<ServerInterceptor> interceptors) {
        this.properties = properties;
        this.bindableServices = bindableServices;
        this.interceptors = new ArrayList<>(interceptors);
        AnnotationAwareOrderComparator.sort(this.interceptors);
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        NettyServerBuilder builder = NettyServerBuilder.forPort(properties.getPort());
        ServerInterceptor[] interceptorArray = interceptors.toArray(ServerInterceptor[]::new);
        for (BindableService service : bindableServices) {
            builder.addService(ServerInterceptors.intercept(service, interceptorArray));
        }
        HealthStatusManager healthStatusManager = null;
        if (properties.isHealthEnabled()) {
            healthStatusManager = new HealthStatusManager();
            builder.addService(healthStatusManager.getHealthService());
        }
        if (properties.isReflectionEnabled()) {
            builder.addService(ProtoReflectionService.newInstance());
        }
        try {
            server = builder.build().start();
            if (healthStatusManager != null) {
                for (BindableService service : bindableServices) {
                    ServerServiceDefinition definition = service.bindService();
                    healthStatusManager.setStatus(definition.getServiceDescriptor().getName(),
                            HealthCheckResponse.ServingStatus.SERVING);
                }
            }
            running = true;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start gRPC server on port " + properties.getPort(), ex);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
