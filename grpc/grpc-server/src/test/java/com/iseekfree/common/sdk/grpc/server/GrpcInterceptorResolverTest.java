package com.iseekfree.common.sdk.grpc.server;

import com.iseekfree.common.sdk.grpc.server.service.GrpcService;
import io.grpc.BindableService;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrpcInterceptorResolverTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Beans.class);

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void declaredInterceptorsAreScopedToTheDeclaringService() {
        List<BindableService> services = List.of(new PlainService(), new ScopedService());
        Set<Class<? extends ServerInterceptor>> scoped = GrpcInterceptorResolver.serviceScoped(services);
        assertEquals(Set.of(ScopedInterceptor.class), scoped);

        ServerInterceptor global = context.getBean(GlobalInterceptor.class);
        ServerInterceptor scopedInterceptor = context.getBean(ScopedInterceptor.class);
        List<ServerInterceptor> globalInterceptors = GrpcInterceptorResolver.global(
                List.of(global, scopedInterceptor), scoped);

        assertEquals(List.of(global), globalInterceptors);
        assertEquals(List.of(global),
                GrpcInterceptorResolver.forService(new PlainService(), globalInterceptors, context));
        assertEquals(List.of(global, scopedInterceptor),
                GrpcInterceptorResolver.forService(new ScopedService(), globalInterceptors, context));
    }

    @Test
    void declaredInterceptorRequiresABean() {
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> GrpcInterceptorResolver.forService(new MissingBeanService(), List.of(), context));
        assertTrue(failure.getMessage().contains(MissingInterceptor.class.getName()));
    }

    @Configuration(proxyBeanMethods = false)
    static class Beans {

        @Bean
        GlobalInterceptor globalInterceptor() {
            return new GlobalInterceptor();
        }

        @Bean
        ScopedInterceptor scopedInterceptor() {
            return new ScopedInterceptor();
        }
    }

    static class GlobalInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                     Metadata headers,
                                                                     ServerCallHandler<ReqT, RespT> next) {
            return next.startCall(call, headers);
        }
    }

    static class ScopedInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                     Metadata headers,
                                                                     ServerCallHandler<ReqT, RespT> next) {
            return next.startCall(call, headers);
        }
    }

    static class MissingInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                     Metadata headers,
                                                                     ServerCallHandler<ReqT, RespT> next) {
            return next.startCall(call, headers);
        }
    }

    static class PlainService implements BindableService {
        @Override
        public ServerServiceDefinition bindService() {
            return ServerServiceDefinition.builder("plain").build();
        }
    }

    @GrpcService(interceptors = ScopedInterceptor.class)
    static class ScopedService implements BindableService {
        @Override
        public ServerServiceDefinition bindService() {
            return ServerServiceDefinition.builder("scoped").build();
        }
    }

    @GrpcService(interceptors = MissingInterceptor.class)
    static class MissingBeanService implements BindableService {
        @Override
        public ServerServiceDefinition bindService() {
            return ServerServiceDefinition.builder("missing").build();
        }
    }
}
