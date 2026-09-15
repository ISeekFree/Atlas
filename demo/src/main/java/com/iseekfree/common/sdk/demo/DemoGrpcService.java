package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.common.ctx.AtlasContext;
import com.iseekfree.common.sdk.demo.grpc.DemoServerInnerAuthInterceptor;
import com.iseekfree.common.sdk.grpc.common.GrpcContext;
import com.iseekfree.common.sdk.grpc.common.GrpcStringMarshaller;
import com.iseekfree.common.sdk.grpc.server.service.GrpcService;
import io.grpc.BindableService;
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;

@GrpcService(interceptors = DemoServerInnerAuthInterceptor.class)
public class DemoGrpcService implements BindableService {

    public static final MethodDescriptor<String, String> METHOD = MethodDescriptor.<String, String>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(MethodDescriptor.generateFullMethodName("demo.Echo", "Say"))
            .setRequestMarshaller(new GrpcStringMarshaller())
            .setResponseMarshaller(new GrpcStringMarshaller())
            .build();

    @Override
    public ServerServiceDefinition bindService() {
        return ServerServiceDefinition.builder("demo.Echo")
                .addMethod(METHOD, ServerCalls.asyncUnaryCall((request, observer) -> {
                    String uid = GrpcContext.current().map(AtlasContext::getUid).orElse("anonymous");
                    observer.onNext("echo:" + uid + ":" + request);
                    observer.onCompleted();
                }))
                .build();
    }
}
