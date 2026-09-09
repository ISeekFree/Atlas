package com.iseekfree.common.sdk.web.context;

import java.util.Optional;

public final class WebContextHolder {

    private static final ThreadLocal<WebContext> HOLDER = new ThreadLocal<>();

    private WebContextHolder() {
    }

    public static Optional<WebContext> current() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static WebContext currentOrEmpty() {
        WebContext context = HOLDER.get();
        return context == null ? new WebContext() : context;
    }

    public static void set(WebContext context) {
        HOLDER.set(context);
    }

    public static void clear() {
        HOLDER.remove();
    }
}
