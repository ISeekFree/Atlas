package com.iseekfree.common.sdk.grpc.server;

import com.iseekfree.common.sdk.grpc.server.service.GrpcService;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves the {@link ServerInterceptor}s mounted on each {@link BindableService}.
 *
 * <p>Interceptors declared on {@link GrpcService#interceptors()} are scoped to
 * the declaring service: they are resolved from the Spring context and excluded
 * from the global set, so a shared interceptor bean can be reused without
 * leaking onto services that never asked for it.</p>
 */
final class GrpcInterceptorResolver {

    private GrpcInterceptorResolver() {
    }

    /** Every interceptor type declared on any of the given services. */
    static Set<Class<? extends ServerInterceptor>> serviceScoped(Collection<BindableService> services) {
        Set<Class<? extends ServerInterceptor>> scoped = new LinkedHashSet<>();
        for (BindableService service : services) {
            scoped.addAll(declared(service));
        }
        return scoped;
    }

    /** The candidates that are mounted on every service, in order. */
    static List<ServerInterceptor> global(List<ServerInterceptor> candidates,
                                          Set<Class<? extends ServerInterceptor>> scoped) {
        List<ServerInterceptor> global = new ArrayList<>();
        for (ServerInterceptor interceptor : candidates) {
            if (!isScoped(interceptor, scoped)) {
                global.add(interceptor);
            }
        }
        AnnotationAwareOrderComparator.sort(global);
        return global;
    }

    /** The interceptors for one service: the global set plus its declared ones. */
    static List<ServerInterceptor> forService(BindableService service,
                                              List<ServerInterceptor> global,
                                              ApplicationContext context) {
        List<ServerInterceptor> interceptors = new ArrayList<>(global);
        for (Class<? extends ServerInterceptor> type : declared(service)) {
            if (interceptors.stream().anyMatch(type::isInstance)) {
                continue;
            }
            interceptors.add(resolve(type, context));
        }
        AnnotationAwareOrderComparator.sort(interceptors);
        return interceptors;
    }

    private static List<Class<? extends ServerInterceptor>> declared(BindableService service) {
        if (service == null) {
            return List.of();
        }
        GrpcService annotation = AnnotationUtils.findAnnotation(service.getClass(), GrpcService.class);
        if (annotation == null) {
            return List.of();
        }
        return List.of(annotation.interceptors());
    }

    private static ServerInterceptor resolve(Class<? extends ServerInterceptor> type, ApplicationContext context) {
        try {
            return context.getBean(type);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new IllegalStateException("@GrpcService(interceptors = " + type.getName()
                    + ".class) requires a Spring bean of that type", ex);
        }
    }

    private static boolean isScoped(ServerInterceptor interceptor,
                                    Set<Class<? extends ServerInterceptor>> scoped) {
        for (Class<? extends ServerInterceptor> type : scoped) {
            if (type.isInstance(interceptor)) {
                return true;
            }
        }
        return false;
    }
}
