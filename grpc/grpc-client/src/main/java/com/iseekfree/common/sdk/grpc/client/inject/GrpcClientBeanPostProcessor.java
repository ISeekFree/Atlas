package com.iseekfree.common.sdk.grpc.client.inject;

import com.iseekfree.common.sdk.grpc.client.GrpcChannelFactory;
import io.grpc.Channel;
import io.grpc.stub.AbstractStub;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class GrpcClientBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {

    private final ObjectProvider<GrpcChannelFactory> channelFactory;

    public GrpcClientBeanPostProcessor(ObjectProvider<GrpcChannelFactory> channelFactory) {
        this.channelFactory = channelFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> target = bean.getClass();
        while (target != null && target != Object.class) {
            ReflectionUtils.doWithFields(target, field -> inject(bean, field), field -> field.isAnnotationPresent(GrpcClient.class));
            target = target.getSuperclass();
        }
        return bean;
    }

    private void inject(Object bean, Field field) {
        GrpcClient annotation = field.getAnnotation(GrpcClient.class);
        Channel channel = channelFactory.getObject().channel(annotation.value());
        Object value = Channel.class.isAssignableFrom(field.getType()) ? channel : createStub(field.getType(), channel);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, bean, value);
    }

    private Object createStub(Class<?> fieldType, Channel channel) {
        if (!AbstractStub.class.isAssignableFrom(fieldType)) {
            throw new IllegalArgumentException("@GrpcClient field must be a Channel or AbstractStub: " + fieldType.getName());
        }
        Class<?> grpcClass = fieldType.getEnclosingClass();
        if (grpcClass == null) {
            throw new IllegalArgumentException("Cannot resolve generated gRPC enclosing class for " + fieldType.getName());
        }
        for (Method method : grpcClass.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !fieldType.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length == 1 && Channel.class.isAssignableFrom(parameters[0])) {
                return ReflectionUtils.invokeMethod(method, null, channel);
            }
        }
        throw new IllegalArgumentException("No generated new*Stub(Channel) factory found for " + fieldType.getName());
    }

    @Override
    public int getOrder() {
        return PriorityOrdered.LOWEST_PRECEDENCE;
    }
}
