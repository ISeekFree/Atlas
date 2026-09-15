package com.iseekfree.common.sdk.web.mvc;

import com.iseekfree.common.sdk.common.ctx.WebContext;
import com.iseekfree.common.sdk.web.context.WebContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Injects the request's {@link WebContext} into a controller parameter.
 *
 * <p>Any {@link WebContext} subclass qualifies, so an application can require
 * its own {@code NexusWebContext} parameter and call {@code ctx.getXxx()}
 * directly. When the registered {@code WebContextProvider} returned the base
 * type, the resolver instantiates the parameter type and copies the base fields
 * over.</p>
 */
public class WebContextArgumentResolver implements HandlerMethodArgumentResolver {

    private final WebContextFactory contextFactory;

    public WebContextArgumentResolver(WebContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return WebContext.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        WebContext context = contextFactory.create(request, response);
        Class<?> parameterType = parameter.getParameterType();
        if (parameterType.isInstance(context)) {
            return context;
        }
        return copyInto(parameterType, context);
    }

    private WebContext copyInto(Class<?> parameterType, WebContext context) {
        try {
            WebContext target = (WebContext) parameterType.getDeclaredConstructor().newInstance();
            context.copyTo(target);
            return target;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Cannot instantiate WebContext parameter type " + parameterType.getName()
                    + "; register a WebContextProvider returning it instead", ex);
        }
    }
}
