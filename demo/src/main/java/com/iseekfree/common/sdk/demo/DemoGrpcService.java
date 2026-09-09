package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.grpc.common.GrpcAuthContext;
import com.iseekfree.common.sdk.grpc.common.GrpcStringMarshaller;
import com.iseekfree.common.sdk.grpc.server.service.GrpcService;
import io.grpc.BindableService;
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;

@GrpcService
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
                    String uid = GrpcAuthContext.userId().orElse("anonymous");
                    observer.onNext("echo:" + uid + ":" + request);
                    observer.onCompleted();
                }))
                .build();
    }
}
