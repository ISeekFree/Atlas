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
        assertResolvedAddress("static://127.0.0.1:16814", "127.0.0.1", 16814);
    }

    @Test
    void resolvesStaticDomainAndIpv6() {
        assertResolvedAddress("static://account.internal:16814", "account.internal", 16814);
        assertResolvedAddress("static://[::1]:16814", "::1", 16814);
    }

    @Test
    void resolvesTargetNormalizedFromMissingScheme() {
        assertResolvedAddress("static:///account.internal:16814", "account.internal", 16814);
    }

    @Test
    void registersSupportedSchemesAndStaticAsDefault() {
        new GrpcChannelFactory(new com.iseekfree.common.sdk.grpc.client.autoconfigure.AtlasGrpcClientProperties(), List.of());

        NameResolverRegistry registry = NameResolverRegistry.getDefaultRegistry();
        assertThat(registry.getProviderForScheme("static")).isNotNull();
        assertThat(registry.getProviderForScheme("dns")).isNotNull();
        assertThat(registry.getProviderForScheme("xds")).isNotNull();
        assertThat(registry.getProviderForScheme("unix")).isNotNull();
        assertThat(registry.getDefaultScheme()).isEqualTo("static");
    }

    @Test
    void rejectsMissingPort() {
        StaticNameResolverProvider provider = new StaticNameResolverProvider();

        assertThatThrownBy(() -> provider.newNameResolver(URI.create("static://127.0.0.1"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("static://host:port");
    }

    private static void assertResolvedAddress(String target, String expectedHost, int expectedPort) {
        StaticNameResolverProvider provider = new StaticNameResolverProvider();
        NameResolver resolver = provider.newNameResolver(URI.create(target), null);
        RecordingListener listener = new RecordingListener();

        resolver.start(listener);

        assertThat(listener.addresses).hasSize(1);
        InetSocketAddress address = (InetSocketAddress) listener.addresses.get(0).getAddresses().get(0);
        if (expectedHost.equals("::1")) {
            assertThat(address.getAddress().isLoopbackAddress()).isTrue();
        } else {
            assertThat(address.getHostString()).isEqualTo(expectedHost);
        }
        assertThat(address.getPort()).isEqualTo(expectedPort);
        if (expectedHost.equals("127.0.0.1") || expectedHost.equals("::1")) {
            assertThat(address.isUnresolved()).isFalse();
        }
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
