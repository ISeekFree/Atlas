package com.iseekfree.common.sdk.grpc.common;

import com.iseekfree.common.sdk.common.ctx.WebContextRequest;
import com.iseekfree.common.sdk.common.net.ClientIp;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Adapts gRPC {@link Metadata} onto the transport-neutral
 * {@link WebContextRequest}, so the app's {@code WebContextLoader} parses a gRPC
 * token exactly like it parses an HTTP one. gRPC metadata keys are lower-case;
 * lookups lower-case the requested name to match.
 */
public class GrpcWebContextRequest implements WebContextRequest {

    private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9_.-]+");

    private final Metadata headers;
    private final String remoteIp;

    public GrpcWebContextRequest(Metadata headers) {
        this(headers, null);
    }

    public GrpcWebContextRequest(Metadata headers, String remoteIp) {
        this.headers = headers;
        this.remoteIp = remoteIp;
    }

    /** Builds a request, deriving the peer IP from the server call attributes. */
    public static GrpcWebContextRequest of(ServerCall<?, ?> call, Metadata headers) {
        return new GrpcWebContextRequest(headers, resolveIp(call, headers));
    }

    @Override
    public String header(String name) {
        if (headers == null || name == null) {
            return null;
        }
        String key = name.toLowerCase(Locale.ROOT);
        if (!VALID_KEY.matcher(key).matches()) {
            return null;
        }
        return headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
    }

    @Override
    public String remoteIp() {
        return remoteIp;
    }

    private static String resolveIp(ServerCall<?, ?> call, Metadata headers) {
        String forwarded = headers == null ? null : headers.get(GrpcMetadataKeys.X_FORWARDED_FOR);
        String real = headers == null ? null : headers.get(GrpcMetadataKeys.X_REAL_IP);
        String ip = firstNonBlank(forwarded, real);
        if (ip != null && ip.contains(",")) {
            return ClientIp.normalize(ip.split(",")[0].trim());
        }
        if (ip != null) {
            return ClientIp.normalize(ip);
        }
        Object remoteAddress = call == null ? null : call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        if (remoteAddress instanceof InetSocketAddress socket && socket.getAddress() != null) {
            return ClientIp.normalize(socket.getAddress().getHostAddress());
        }
        return remoteAddress == null ? null : ClientIp.normalize(remoteAddress.toString());
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
