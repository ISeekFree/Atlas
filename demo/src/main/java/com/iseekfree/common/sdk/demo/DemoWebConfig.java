package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.common.ctx.WebContextAuthorizer;
import com.iseekfree.common.sdk.web.context.WebContextFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Mounts the demo's business-owned HTTP auth interceptor.
 *
 * <p>The exclusion list and the {@code requiredByDefault} switch live here, in
 * the application, not in the SDK.</p>
 */
@Configuration(proxyBeanMethods = false)
public class DemoWebConfig {

    @Bean
    public DemoWebAuthInterceptor demoWebAuthInterceptor(WebContextFactory contextFactory,
                                                         ObjectProvider<WebContextAuthorizer> authorizer) {
        return new DemoWebAuthInterceptor(contextFactory, authorizer.getIfAvailable(), false);
    }

    @Bean
    public WebMvcConfigurer demoWebMvcConfigurer(DemoWebAuthInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor)
                        .excludePathPatterns("/favicon.ico", "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg")
                        .order(Ordered.LOWEST_PRECEDENCE);
            }
        };
    }
}
