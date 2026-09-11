package com.iseekfree.common.sdk.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iseekfree.common.sdk.common.auth.AuthService;
import com.iseekfree.common.sdk.common.auth.CookieStyleAuthService;
import com.iseekfree.common.sdk.common.json.Jsons;
import com.iseekfree.common.sdk.web.context.WebContextFactory;
import com.iseekfree.common.sdk.web.context.WebContextCustomizer;
import com.iseekfree.common.sdk.web.mvc.ApiResponseAdvice;
import com.iseekfree.common.sdk.web.mvc.GlobalExceptionHandler;
import com.iseekfree.common.sdk.web.mvc.WebAuthInterceptor;
import com.iseekfree.common.sdk.web.mvc.WebContextArgumentResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnProperty(prefix = "framework.web", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AtlasWebProperties.class)
public class AtlasWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper defaultObjectMapper() {
        return Jsons.OBJECT_MAPPER;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthService defaultAuthService() {
        return new CookieStyleAuthService();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebContextFactory webContextFactory(AtlasWebProperties properties, List<WebContextCustomizer> customizers) {
        return new WebContextFactory(properties, customizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebAuthInterceptor webAuthInterceptor(AtlasWebProperties properties, WebContextFactory contextFactory, AuthService authService) {
        return new WebAuthInterceptor(properties, contextFactory, authService);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebContextArgumentResolver webContextArgumentResolver(WebContextFactory contextFactory) {
        return new WebContextArgumentResolver(contextFactory);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(WebAuthInterceptor webAuthInterceptor, WebContextArgumentResolver argumentResolver, AtlasWebProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
                configurer.defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(webAuthInterceptor)
                        .excludePathPatterns(properties.getAuth().getExcludedPatterns())
                        .order(Ordered.LOWEST_PRECEDENCE);
            }

            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(0, argumentResolver);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiResponseAdvice apiResponseAdvice(AtlasWebProperties properties) {
        return new ApiResponseAdvice(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
