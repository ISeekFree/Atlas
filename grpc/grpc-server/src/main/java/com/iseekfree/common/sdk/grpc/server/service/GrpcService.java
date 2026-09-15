package com.iseekfree.common.sdk.grpc.server.service;

import io.grpc.ServerInterceptor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GrpcService {

    /**
     * Server interceptors applied to this service only.
     *
     * <p>Each referenced type must be an {@link ServerInterceptor} Spring bean.
     * Declared interceptors are excluded from the global interceptor set, so a
     * shared bean is mounted on exactly the services that declare it instead of
     * every {@code @GrpcService} in the application.</p>
     */
    Class<? extends ServerInterceptor>[] interceptors() default {};
}
