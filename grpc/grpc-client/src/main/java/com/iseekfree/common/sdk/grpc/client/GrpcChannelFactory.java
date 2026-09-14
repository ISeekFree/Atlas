package com.iseekfree.common.sdk.grpc.client;

import com.iseekfree.common.sdk.grpc.client.autoconfigure.AtlasGrpcClientProperties;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GrpcChannelFactory implements DisposableBean {

    static {
        NameResolverRegistry registry = NameResolverRegistry.getDefaultRegistry();
        if (registry.getProviderForScheme(StaticNameResolverProvider.SCHEME) == null) {
            registry.register(new StaticNameResolverProvider());
        }
    }

    private final AtlasGrpcClientProperties properties;
    private final List<ClientInterceptor> interceptors;
    private final Map<String, ManagedChannel> channels = new LinkedHashMap<>();

    public GrpcChannelFactory(AtlasGrpcClientProperties properties, List<ClientInterceptor> interceptors) {
        this.properties = properties;
        this.interceptors = new ArrayList<>(interceptors);
        AnnotationAwareOrderComparator.sort(this.interceptors);
    }

    public Channel channel(String name) {
        ManagedChannel managedChannel = managedChannel(name);
        if (interceptors.isEmpty()) {
            return managedChannel;
        }
        return ClientInterceptors.intercept(managedChannel, interceptors);
    }

    public ManagedChannel managedChannel(String name) {
        return channels.computeIfAbsent(name, this::createManagedChannel);
    }

    private ManagedChannel createManagedChannel(String name) {
        AtlasGrpcClientProperties.Channel config = properties.getChannels().get(name);
        if (config == null || config.getTarget() == null || config.getTarget().isBlank()) {
            throw new IllegalArgumentException("Missing framework.grpc.client.channels." + name + ".target");
        }
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(config.getTarget());
        if (config.isPlaintext()) {
            builder.usePlaintext();
        }
        if (config.getMaxInboundMessageSize() != null) {
            builder.maxInboundMessageSize(config.getMaxInboundMessageSize());
        }
        return builder.build();
    }

    @Override
    public void destroy() throws Exception {
        for (ManagedChannel channel : channels.values()) {
            channel.shutdown();
        }
        for (ManagedChannel channel : channels.values()) {
            if (!channel.awaitTermination(3, TimeUnit.SECONDS)) {
                channel.shutdownNow();
            }
        }
    }
}
