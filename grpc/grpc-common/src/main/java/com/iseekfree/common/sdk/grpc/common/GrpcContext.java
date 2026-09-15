package com.iseekfree.common.sdk.grpc.common;

import com.iseekfree.common.sdk.common.ctx.AtlasContext;
import io.grpc.Context;

import java.util.Optional;

/**
 * gRPC transport context: an {@link AtlasContext} sibling of the HTTP
 * {@link com.iseekfree.common.sdk.common.ctx.WebContext} and the WebSocket
 * context, plus the carrier that keeps it on the gRPC call.
 *
 * <p>An application may use it as is or extend it with gRPC-specific fields. A
 * server interceptor resolves the context (typically through the same
 * {@code WebContextLoader} used everywhere else) and attaches it with
 * {@link #attach(Context, AtlasContext)}; the service method reads it back with
 * {@link #current()} and calls {@code ctx.getUid()} like a controller does.</p>
 */
public class GrpcContext extends AtlasContext {

    public static final Context.Key<AtlasContext> KEY = Context.key("atlas.grpc.context");

    /** The context attached to the current gRPC call, if any. */
    public static Optional<AtlasContext> current() {
        return Optional.ofNullable(KEY.get());
    }

    /** Attaches a resolved context to the given gRPC context. */
    public static Context attach(Context context, AtlasContext atlasContext) {
        return context.withValue(KEY, atlasContext);
    }
}
