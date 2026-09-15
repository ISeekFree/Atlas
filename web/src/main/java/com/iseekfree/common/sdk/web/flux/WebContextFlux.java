package com.iseekfree.common.sdk.web.flux;

import com.iseekfree.common.sdk.common.ctx.AtlasContext;
import com.iseekfree.common.sdk.common.ctx.WebContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class WebContextFlux {

    private WebContextFlux() {
    }

    public static <T> Flux<T> withContext(Flux<T> flux, AtlasContext context) {
        return flux.contextWrite(reactorContext -> reactorContext.put(AtlasContext.class, context));
    }

    public static <T> Mono<T> withContext(Mono<T> mono, AtlasContext context) {
        return mono.contextWrite(reactorContext -> reactorContext.put(AtlasContext.class, context));
    }

    public static Mono<AtlasContext> current() {
        return Mono.deferContextual(contextView -> {
            if (contextView.hasKey(AtlasContext.class)) {
                return Mono.just(contextView.get(AtlasContext.class));
            }
            return WebContextHolder.current().map(Mono::just).orElseGet(Mono::empty);
        });
    }
}
