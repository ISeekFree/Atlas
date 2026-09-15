package com.iseekfree.common.sdk.web.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iseekfree.common.sdk.common.auth.JwtCodec;
import com.iseekfree.common.sdk.common.ctx.WebContextAuthorizer;
import com.iseekfree.common.sdk.common.ctx.WebContextLoader;
import com.iseekfree.common.sdk.common.ctx.WebContextProvider;
import com.iseekfree.common.sdk.common.json.Jsons;
import com.iseekfree.common.sdk.web.context.WebContextCustomizer;
import com.iseekfree.common.sdk.web.context.WebContextFactory;
import com.iseekfree.common.sdk.web.mvc.ApiResponseAdvice;
import com.iseekfree.common.sdk.web.mvc.ExceptionResponseResolver;
import com.iseekfree.common.sdk.web.mvc.GlobalExceptionHandler;
import com.iseekfree.common.sdk.web.mvc.WebContextArgumentResolver;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

/**
 * Wires the transport-neutral {@code ctx} package into Spring MVC.
 *
 * <p>The SDK ships no {@code AuthService}, no identity model and no HTTP auth
 * interceptor: applications register a {@link WebContextProvider} for their
 * context subtype and one or more {@link WebContextLoader} beans to parse the
 * token, then enforce their own policy in a business-owned
 * {@code HandlerInterceptor}. The shared {@link WebContextFactory} builds the
 * context and {@link WebContextArgumentResolver} injects it into controller
 * parameters; permission checks for {@code @AuthRequired(perms = ...)} can be
 * delegated to an optional {@link WebContextAuthorizer}.</p>
 */
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

    /**
     * Makes the Spring MVC Jackson mapper (Jackson 3 by default in Spring Boot 4)
     * omit {@code null} members, so every controller response matches the
     * {@link com.iseekfree.common.sdk.common.web.Response} envelope and never
     * serializes {@code "field":null}. Applications can override it with their
     * own {@link JsonMapperBuilderCustomizer}.
     */
    @Bean
    @ConditionalOnClass(JsonMapperBuilderCustomizer.class)
    @ConditionalOnMissingBean(name = "atlasJsonNullOmissionCustomizer")
    public JsonMapperBuilderCustomizer atlasJsonNullOmissionCustomizer() {
        return builder -> builder.changeDefaultPropertyInclusion(
                inclusion -> inclusion.withValueInclusion(JsonInclude.Include.NON_NULL));
    }

    /**
     * Convenience HMAC {@link JwtCodec} built from {@code framework.web.auth.jwt.secret}.
     * It performs crypto only; applications verify tokens inside their
     * {@link WebContextLoader} and may replace this bean with any other codec.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "framework.web.auth.jwt", name = "secret")
    public JwtCodec jwtCodec(AtlasWebProperties properties) {
        SecretKey key = Keys.hmacShaKeyFor(
                properties.getAuth().getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
        return JwtCodec.hmac(key);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebContextFactory webContextFactory(AtlasWebProperties properties,
                                               ObjectProvider<WebContextProvider> provider,
                                               List<WebContextLoader> loaders,
                                               List<WebContextCustomizer> customizers) {
        return new WebContextFactory(properties, provider.getIfAvailable(), loaders, customizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebContextArgumentResolver webContextArgumentResolver(WebContextFactory contextFactory) {
        return new WebContextArgumentResolver(contextFactory);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(WebContextArgumentResolver argumentResolver) {
        return new WebMvcConfigurer() {
            @Override
            public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
                configurer.defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON);
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

    /**
     * Registers the SDK-wide CORS policy as an early servlet filter, so
     * preflight requests are answered before the application's auth interceptor
     * and errors still carry the CORS headers. Configure it with
     * {@code framework.web.cors.*}.
     */
    @Bean
    @ConditionalOnMissingBean(name = "corsFilter")
    @ConditionalOnProperty(prefix = "framework.web.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<CorsFilter> corsFilter(AtlasWebProperties properties) {
        AtlasWebProperties.Cors cors = properties.getCors();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(cors.getAllowedOriginPatterns());
        cors.getAllowedMethods().forEach(configuration::addAllowedMethod);
        cors.getAllowedHeaders().forEach(configuration::addAllowedHeader);
        cors.getExposedHeaders().forEach(configuration::addExposedHeader);
        configuration.setAllowCredentials(cors.isAllowCredentials());
        configuration.setMaxAge(cors.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(cors.getPathPattern(), configuration);

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(List<ExceptionResponseResolver> resolvers) {
        return new GlobalExceptionHandler(resolvers);
    }
}
