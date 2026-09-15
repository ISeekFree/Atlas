package com.iseekfree.common.sdk.demo.grpc;

import com.iseekfree.common.sdk.demo.DemoContextLoader;
import com.iseekfree.common.sdk.grpc.common.GrpcMetadataKeys;
import com.iseekfree.common.sdk.grpc.common.GrpcContext;
import com.iseekfree.common.sdk.grpc.common.GrpcWebContextRequest;
import com.iseekfree.common.sdk.grpc.server.service.GrpcService;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Inbound gRPC auth for the demo application.
 *
 * <p>The SDK ships no server-side auth interceptor on purpose: token resolution
 * and trust stay in the business project. This interceptor reuses the exact same
 * {@link DemoContextLoader} used by HTTP, adapting the call to a
 * {@link GrpcWebContextRequest}, and attaches the resulting context with
 * {@link GrpcContext#attach}. Declare it on a service with
 * {@code @GrpcService(interceptors = DemoServerInnerAuthInterceptor.class)}.</p>
 */
@Service
public class DemoServerInnerAuthInterceptor implements ServerInterceptor {

    private final DemoContextLoader contextLoader;
    private final boolean required;
    private final String innerToken;

    public DemoServerInnerAuthInterceptor(DemoContextLoader contextLoader,
                                          @Value("${framework.grpc.server.auth.required:true}") boolean required,
                                          @Value("${framework.grpc.server.auth.inner-token:}") String innerToken) {
        this.contextLoader = contextLoader;
        this.required = required;
        this.innerToken = innerToken;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        GrpcWebContextRequest request = GrpcWebContextRequest.of(call, headers);
        GrpcContext context = new GrpcContext();
        context.setIp(request.remoteIp());
        if (matchesInnerToken(headers.get(GrpcMetadataKeys.GRPC_TOKEN))) {
            context.setUid("inner");
            context.setDomain("grpc-inner");
        } else {
            contextLoader.load(context, request);
        }
        if (required && (context.getUid() == null || context.getUid().isBlank())) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing gRPC token"), new Metadata());
            return new ServerCall.Listener<>() {
            };
        }
        Context grpcContext = GrpcContext.attach(Context.current(), context);
        return Contexts.interceptCall(grpcContext, call, headers, next);
    }

    private boolean matchesInnerToken(String token) {
        return innerToken != null && !innerToken.isBlank() && innerToken.equals(token);
    }
}
