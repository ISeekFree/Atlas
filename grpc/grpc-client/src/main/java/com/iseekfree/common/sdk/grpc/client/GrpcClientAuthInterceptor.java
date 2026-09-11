package com.iseekfree.common.sdk.grpc.client;

import com.iseekfree.common.sdk.grpc.common.GrpcMetadataKeys;
import com.iseekfree.common.sdk.web.context.WebContext;
import com.iseekfree.common.sdk.web.context.WebContextHolder;
import com.iseekfree.common.sdk.web.autoconfigure.AtlasWebProperties;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.util.List;

public class GrpcClientAuthInterceptor implements ClientInterceptor {

    private final List<String> tokenHeaders;
    private final List<String> adminTokenHeaders;

    public GrpcClientAuthInterceptor() {
        this(null);
    }

    public GrpcClientAuthInterceptor(AtlasWebProperties webProperties) {
        if (webProperties == null) {
            this.tokenHeaders = List.of("token", "Authorization", "accessToken");
            this.adminTokenHeaders = List.of("adminToken");
            return;
        }
        this.tokenHeaders = List.copyOf(webProperties.getAuth().getTokenHeaders());
        this.adminTokenHeaders = List.copyOf(webProperties.getAuth().getAdminTokenHeaders());
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);
        return new ForwardingClientCall.SimpleForwardingClientCall<>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                WebContextHolder.current()
                        .map(GrpcClientAuthInterceptor.this::resolveToken)
                        .filter(token -> !token.isBlank())
                        .ifPresent(token -> {
                            headers.put(GrpcMetadataKeys.AUTHORIZATION, token);
                            headers.put(GrpcMetadataKeys.ACCESS_TOKEN, token);
                        });
                super.start(responseListener, headers);
            }
        };
    }

    private String resolveToken(WebContext context) {
        if (context.getRequest() == null) {
            return null;
        }
        for (String header : adminTokenHeaders) {
            String token = context.getRequest().getHeader(header);
            if (token != null && !token.isBlank()) {
                return token;
            }
        }
        for (String header : tokenHeaders) {
            String token = context.getRequest().getHeader(header);
            if (token != null && !token.isBlank()) {
                return token;
            }
        }
        return null;
    }
}
