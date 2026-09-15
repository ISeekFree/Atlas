package com.iseekfree.common.sdk.common.ctx;

/**
 * Parses a request into the context (token, tenant, permissions, ...). Loaders
 * are transported-neutral so the same implementation serves HTTP, gRPC and
 * WebSocket. Registering no loader leaves the context anonymous.
 */
@FunctionalInterface
public interface WebContextLoader {

    void load(AtlasContext context, WebContextRequest request);
}
