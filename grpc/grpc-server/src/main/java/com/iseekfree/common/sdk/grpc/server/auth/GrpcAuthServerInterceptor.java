package com.iseekfree.common.sdk.grpc.server.auth;

import com.iseekfree.common.sdk.common.auth.AuthIdentity;
import com.iseekfree.common.sdk.common.auth.AuthRequest;
import com.iseekfree.common.sdk.common.auth.AuthService;
import com.iseekfree.common.sdk.grpc.common.GrpcAuthContext;
import com.iseekfree.common.sdk.grpc.common.GrpcMetadataKeys;
import com.iseekfree.common.sdk.grpc.server.autoconfigure.AtlasGrpcServerProperties;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

public class GrpcAuthServerInterceptor implements ServerInterceptor {

    private final AtlasGrpcServerProperties properties;
    private final AuthService authService;

    public GrpcAuthServerInterceptor(AtlasGrpcServerProperties properties, AuthService authService) {
        this.properties = properties;
        this.authService = authService;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String ip = resolveIp(call, headers);
        String innerToken = headers.get(GrpcMetadataKeys.GRPC_TOKEN);
        if (matchesInnerToken(innerToken)) {
            AuthIdentity identity = AuthIdentity.builder("inner")
                    .domain("grpc-inner")
                    .claim("authType", "inner")
                    .build();
            Context context = GrpcAuthContext.withIdentity(Context.current(), identity, ip);
            return Contexts.interceptCall(context, call, headers, next);
        }

        String token = firstNonBlank(headers.get(GrpcMetadataKeys.ACCESS_TOKEN), headers.get(GrpcMetadataKeys.AUTHORIZATION));
        if (token == null) {
            if (properties.getAuth().isRequired()) {
                close(call, Status.UNAUTHENTICATED.withDescription("Missing gRPC token"));
                return new ServerCall.Listener<>() {
                };
            }
            return next.startCall(call, headers);
        }

        try {
            AuthIdentity identity = authService.authenticate(AuthRequest.builder().token(token).ip(ip).build());
            Context context = GrpcAuthContext.withIdentity(Context.current(), identity, ip);
            return Contexts.interceptCall(context, call, headers, next);
        } catch (RuntimeException ex) {
            close(call, Status.UNAUTHENTICATED.withDescription(ex.getMessage()));
            return new ServerCall.Listener<>() {
            };
        }
    }

    private boolean matchesInnerToken(String token) {
        String expected = properties.getAuth().getInnerToken();
        return expected != null && !expected.isBlank() && expected.equals(token);
    }

    private String resolveIp(ServerCall<?, ?> call, Metadata headers) {
        String ip = firstNonBlank(headers.get(GrpcMetadataKeys.X_FORWARDED_FOR), headers.get(GrpcMetadataKeys.X_REAL_IP));
        if (ip != null && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }
        if (ip != null) {
            return ip;
        }
        Object remoteAddress = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        return remoteAddress == null ? "" : remoteAddress.toString();
    }

    private static void close(ServerCall<?, ?> call, Status status) {
        call.close(status, new Metadata());
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
