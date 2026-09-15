package com.iseekfree.common.sdk.web.context;

import com.iseekfree.common.sdk.common.ctx.WebContext;
import com.iseekfree.common.sdk.common.ctx.WebContextHolder;
import com.iseekfree.common.sdk.common.ctx.AtlasContext;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * WebSocket transport context: an {@link AtlasContext} sibling of the HTTP
 * {@link WebContext} and the gRPC context, plus the carrier that keeps it on the
 * session.
 *
 * <p>A session outlives a single handler call, so the context is stored in the
 * session's attribute map (typically during the handshake) and re-bound to the
 * thread while a message is processed. An application may use this class as is or
 * extend it with WebSocket-specific fields.</p>
 */
public class WebsocketContext extends AtlasContext {

    public static final String ATTRIBUTE = "atlas.websocket.context";

    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /** Stores the resolved context on the session attributes. */
    public static void attach(Map<String, Object> attributes, AtlasContext context) {
        if (attributes != null && context != null) {
            attributes.put(ATTRIBUTE, context);
        }
    }

    /** The context stored on the session attributes, if any. */
    public static Optional<AtlasContext> current(Map<String, Object> attributes) {
        if (attributes == null) {
            return Optional.empty();
        }
        Object context = attributes.get(ATTRIBUTE);
        return context instanceof AtlasContext atlasContext ? Optional.of(atlasContext) : Optional.empty();
    }

    /** Binds the session context to the current thread for the duration of the action. */
    public static <T> T callWith(Map<String, Object> attributes, Supplier<T> action) {
        AtlasContext context = current(attributes).orElse(null);
        return context == null ? action.get() : WebContextHolder.callWith(context, action);
    }

    public static void runWith(Map<String, Object> attributes, Runnable action) {
        callWith(attributes, () -> {
            action.run();
            return null;
        });
    }
}
