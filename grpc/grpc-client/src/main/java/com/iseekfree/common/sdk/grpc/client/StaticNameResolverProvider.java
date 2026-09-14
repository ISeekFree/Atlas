package com.iseekfree.common.sdk.grpc.client;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.Status;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

public final class StaticNameResolverProvider extends NameResolverProvider {

    public static final String SCHEME = "static";

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        if (!SCHEME.equals(targetUri.getScheme())) {
            return null;
        }
        String host = targetUri.getHost();
        int port = targetUri.getPort();
        if ((host == null || host.isBlank()) && targetUri.getAuthority() != null) {
            HostAndPort hostAndPort = parseAuthority(targetUri.getAuthority());
            host = hostAndPort.host();
            port = hostAndPort.port();
        }
        if (host == null || host.isBlank() || port < 0) {
            throw new IllegalArgumentException("static target must be static://host:port");
        }
        return new StaticNameResolver(targetUri.getAuthority(), host, port);
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }

    private static HostAndPort parseAuthority(String authority) {
        int separator = authority.lastIndexOf(':');
        if (separator <= 0 || separator == authority.length() - 1) {
            return new HostAndPort(authority, -1);
        }
        String host = authority.substring(0, separator);
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        try {
            return new HostAndPort(host, Integer.parseInt(authority.substring(separator + 1)));
        } catch (NumberFormatException ex) {
            return new HostAndPort(host, -1);
        }
    }

    private record HostAndPort(String host, int port) {
    }

    private static final class StaticNameResolver extends NameResolver {

        private final String authority;
        private final EquivalentAddressGroup addressGroup;

        private StaticNameResolver(String authority, String host, int port) {
            this.authority = authority;
            this.addressGroup = new EquivalentAddressGroup(InetSocketAddress.createUnresolved(host, port));
        }

        @Override
        public String getServiceAuthority() {
            return authority;
        }

        @Override
        public void start(Listener2 listener) {
            listener.onResult(ResolutionResult.newBuilder()
                    .setAddresses(List.of(addressGroup))
                    .setAttributes(Attributes.EMPTY)
                    .build());
        }

        @Override
        public void shutdown() {
        }
    }
}
