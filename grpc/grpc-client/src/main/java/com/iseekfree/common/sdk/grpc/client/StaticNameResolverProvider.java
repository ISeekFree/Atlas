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
        if (host == null || host.isBlank() || port < 0) {
            HostAndPort hostAndPort = parseTarget(targetUri);
            host = hostAndPort.host();
            port = hostAndPort.port();
        }
        host = stripIpv6Brackets(host);
        if (host == null || host.isBlank() || port < 0) {
            throw new IllegalArgumentException("static target must be static://host:port");
        }
        String authority = targetUri.getAuthority();
        if (authority == null || authority.isBlank()) {
            authority = formatAuthority(host, port);
        }
        return new StaticNameResolver(authority, host, port);
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
        // DNS uses priority 5. A higher priority makes static the default for
        // targets without a scheme while explicit dns:/// and other schemes
        // continue to select their own providers.
        return 6;
    }

    private static HostAndPort parseTarget(URI targetUri) {
        String endpoint = targetUri.getAuthority();
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = targetUri.getPath();
        }
        if (endpoint == null) {
            return new HostAndPort(null, -1);
        }
        endpoint = endpoint.trim();
        while (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }
        if (endpoint.startsWith("[")) {
            int closingBracket = endpoint.indexOf(']');
            if (closingBracket <= 1 || closingBracket + 2 >= endpoint.length()
                    || endpoint.charAt(closingBracket + 1) != ':') {
                return new HostAndPort(endpoint, -1);
            }
            return new HostAndPort(endpoint.substring(1, closingBracket),
                    parsePort(endpoint.substring(closingBracket + 2)));
        }
        int separator = endpoint.lastIndexOf(':');
        if (separator <= 0 || separator == endpoint.length() - 1) {
            return new HostAndPort(endpoint, -1);
        }
        return new HostAndPort(endpoint.substring(0, separator),
                parsePort(endpoint.substring(separator + 1)));
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            return port <= 65535 ? port : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static String formatAuthority(String host, int port) {
        return (host.indexOf(':') >= 0 ? "[" + host + "]" : host) + ":" + port;
    }

    private static String stripIpv6Brackets(String host) {
        if (host != null && host.length() > 1 && host.startsWith("[") && host.endsWith("]")) {
            return host.substring(1, host.length() - 1);
        }
        return host;
    }

    private record HostAndPort(String host, int port) {
    }

    private static final class StaticNameResolver extends NameResolver {

        private final String authority;
        private final EquivalentAddressGroup addressGroup;

        private StaticNameResolver(String authority, String host, int port) {
            this.authority = authority;
            this.addressGroup = new EquivalentAddressGroup(new InetSocketAddress(host, port));
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
