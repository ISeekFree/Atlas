package com.iseekfree.common.sdk.web.mvc;

import com.iseekfree.common.sdk.common.json.Jsons;
import com.iseekfree.common.sdk.common.web.Response;
import com.iseekfree.common.sdk.web.autoconfigure.ClawWebProperties;
import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ClawWebProperties properties;

    public ApiResponseAdvice(ClawWebProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return properties.getResponse().isWrap()
                && (AbstractJacksonHttpMessageConverter.class.isAssignableFrom(converterType)
                || AbstractJsonHttpMessageConverter.class.isAssignableFrom(converterType)
                || StringHttpMessageConverter.class.isAssignableFrom(converterType));
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null || body instanceof Response<?> || body instanceof Publisher<?>) {
            return body;
        }
        String path = request.getURI().getPath();
        for (String prefix : properties.getResponse().getNotWrap()) {
            if (path.startsWith(prefix)) {
                return body;
            }
        }
        Response<Object> wrapped = Response.success(body);
        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            return Jsons.toJson(wrapped);
        }
        return wrapped;
    }
}
