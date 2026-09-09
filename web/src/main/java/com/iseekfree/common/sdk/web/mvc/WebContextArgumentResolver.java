package com.iseekfree.common.sdk.web.mvc;

import com.iseekfree.common.sdk.web.context.WebContext;
import com.iseekfree.common.sdk.web.context.WebContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class WebContextArgumentResolver implements HandlerMethodArgumentResolver {

    private final WebContextFactory contextFactory;

    public WebContextArgumentResolver(WebContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return WebContext.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return contextFactory.create(
                webRequest.getNativeRequest(HttpServletRequest.class),
                webRequest.getNativeResponse(HttpServletResponse.class)
        );
    }
}
