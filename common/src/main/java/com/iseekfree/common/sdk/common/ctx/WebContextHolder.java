package com.iseekfree.common.sdk.common.ctx;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Thread-local access to the current {@link AtlasContext} (any transport). HTTP
 * uses the servlet thread directly; gRPC and WebSocket should wrap their handling
 * with {@link #callWith(AtlasContext, Supplier)} /
 * {@link #runWith(AtlasContext, Runnable)}.
 */
public final class WebContextHolder {

    private static final ThreadLocal<AtlasContext> HOLDER = new ThreadLocal<>();

    private WebContextHolder() {
    }

    public static Optional<AtlasContext> current() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static AtlasContext currentOrNull() {
        return HOLDER.get();
    }

    public static AtlasContext currentOrEmpty() {
        AtlasContext context = HOLDER.get();
        return context == null ? new AtlasContext() : context;
    }

    public static void set(AtlasContext context) {
        HOLDER.set(context);
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static <T> T callWith(AtlasContext context, Supplier<T> action) {
        AtlasContext previous = HOLDER.get();
        HOLDER.set(context);
        try {
            return action.get();
        } finally {
            restore(previous);
        }
    }

    public static void runWith(AtlasContext context, Runnable action) {
        callWith(context, () -> {
            action.run();
            return null;
        });
    }

    private static void restore(AtlasContext previous) {
        if (previous == null) {
            HOLDER.remove();
        } else {
            HOLDER.set(previous);
        }
    }
}
