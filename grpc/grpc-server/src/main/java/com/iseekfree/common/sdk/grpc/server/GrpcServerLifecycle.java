package com.iseekfree.common.sdk.grpc.server;

import com.iseekfree.common.sdk.grpc.server.autoconfigure.AtlasGrpcServerProperties;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class GrpcServerLifecycle implements SmartLifecycle {

    private final AtlasGrpcServerProperties properties;
    private final Collection<BindableService> bindableServices;
    private final List<ServerInterceptor> globalInterceptors;
    private final ApplicationContext applicationContext;
    private Server server;
    private boolean running;

    public GrpcServerLifecycle(AtlasGrpcServerProperties properties,
                               Collection<BindableService> bindableServices,
                               List<ServerInterceptor> interceptors,
                               ApplicationContext applicationContext) {
        this.properties = properties;
        this.bindableServices = bindableServices;
        this.applicationContext = applicationContext;
        this.globalInterceptors = GrpcInterceptorResolver.global(
                interceptors, GrpcInterceptorResolver.serviceScoped(bindableServices));
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        NettyServerBuilder builder = NettyServerBuilder.forPort(properties.getPort());
        for (BindableService service : bindableServices) {
            ServerInterceptor[] serviceInterceptors = GrpcInterceptorResolver
                    .forService(service, globalInterceptors, applicationContext)
                    .toArray(ServerInterceptor[]::new);
            builder.addService(ServerInterceptors.intercept(service, serviceInterceptors));
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
