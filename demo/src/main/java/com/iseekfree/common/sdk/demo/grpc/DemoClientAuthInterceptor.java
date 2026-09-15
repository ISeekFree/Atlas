package com.iseekfree.common.sdk.demo.grpc;

import com.iseekfree.common.sdk.grpc.common.GrpcMetadataKeys;
import com.iseekfree.common.sdk.common.ctx.WebContext;
import com.iseekfree.common.sdk.common.ctx.WebContextHolder;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Outbound gRPC auth for the demo application.
 *
 * <p>The SDK no longer ships a client-side auth interceptor: which HTTP header carries the caller
 * token and which metadata key the callee reads are business contracts. This demo declares its own
 * header list, resolves the token from the current {@link WebContext}, and writes it with the
 * shared {@link GrpcMetadataKeys}. Any {@code ClientInterceptor} bean is mounted on every channel by
 * {@code GrpcChannelFactory}.</p>
 */
@Service
public class DemoClientAuthInterceptor implements ClientInterceptor {

    private static final List<String> TOKEN_HEADERS = List.of("token", "Authorization", "accessToken");

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);
        return new ForwardingClientCall.SimpleForwardingClientCall<>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                resolveToken().ifPresent(token -> {
                    headers.put(GrpcMetadataKeys.AUTHORIZATION, token);
                    headers.put(GrpcMetadataKeys.ACCESS_TOKEN, token);
                });
                super.start(responseListener, headers);
            }
        };
    }

    private Optional<String> resolveToken() {
        return WebContextHolder.current()
                .filter(WebContext.class::isInstance)
                .map(WebContext.class::cast)
                .map(WebContext::getRequest)
                .map(request -> TOKEN_HEADERS.stream()
                        .map(request::getHeader)
                        .filter(token -> token != null && !token.isBlank())
                        .findFirst()
                        .orElse(null));
    }
}
