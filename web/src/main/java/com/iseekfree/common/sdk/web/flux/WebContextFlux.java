package com.iseekfree.common.sdk.web.flux;

import com.iseekfree.common.sdk.web.context.WebContext;
import com.iseekfree.common.sdk.web.context.WebContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class WebContextFlux {

    private WebContextFlux() {
    }

    public static <T> Flux<T> withContext(Flux<T> flux, WebContext context) {
        return flux.contextWrite(reactorContext -> reactorContext.put(WebContext.class, context));
    }

    public static <T> Mono<T> withContext(Mono<T> mono, WebContext context) {
        return mono.contextWrite(reactorContext -> reactorContext.put(WebContext.class, context));
    }

    public static Mono<WebContext> current() {
        return Mono.deferContextual(contextView -> {
            if (contextView.hasKey(WebContext.class)) {
                return Mono.just(contextView.get(WebContext.class));
            }
            return WebContextHolder.current().map(Mono::just).orElseGet(Mono::empty);
        });
    }
}
