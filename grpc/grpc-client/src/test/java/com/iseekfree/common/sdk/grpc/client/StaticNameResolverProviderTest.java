package com.iseekfree.common.sdk.grpc.client;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.NameResolverRegistry;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StaticNameResolverProviderTest {

    @Test
    void resolvesStaticHostAndPort() {
        StaticNameResolverProvider provider = new StaticNameResolverProvider();
        NameResolver resolver = provider.newNameResolver(URI.create("static://127.0.0.1:16814"), null);
        RecordingListener listener = new RecordingListener();

        resolver.start(listener);

        assertThat(resolver.getServiceAuthority()).isEqualTo("127.0.0.1:16814");
        assertThat(listener.addresses).hasSize(1);
        InetSocketAddress address = (InetSocketAddress) listener.addresses.get(0).getAddresses().get(0);
        assertThat(address.getHostString()).isEqualTo("127.0.0.1");
        assertThat(address.getPort()).isEqualTo(16814);
    }

    @Test
    void registersStaticSchemeWithDefaultRegistry() {
        new GrpcChannelFactory(new com.iseekfree.common.sdk.grpc.client.autoconfigure.AtlasGrpcClientProperties(), List.of());

        assertThat(NameResolverRegistry.getDefaultRegistry().getProviderForScheme("static")).isNotNull();
    }

    @Test
    void rejectsMissingPort() {
        StaticNameResolverProvider provider = new StaticNameResolverProvider();

        assertThatThrownBy(() -> provider.newNameResolver(URI.create("static://127.0.0.1"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("static://host:port");
    }

    private static final class RecordingListener extends NameResolver.Listener2 {

        private List<EquivalentAddressGroup> addresses = new ArrayList<>();

        @Override
        public void onResult(NameResolver.ResolutionResult resolutionResult) {
            this.addresses = resolutionResult.getAddresses();
        }

        @Override
        public void onError(io.grpc.Status error) {
            throw new AssertionError(error);
        }
    }
}
