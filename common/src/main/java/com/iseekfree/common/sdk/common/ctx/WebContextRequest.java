package com.iseekfree.common.sdk.common.ctx;

/**
 * Transport-neutral view of an incoming request. HTTP headers, gRPC metadata
 * and WebSocket handshake headers all adapt to this interface, so a single
 * {@link WebContextLoader} can parse the token for every transport.
 */
public interface WebContextRequest {

    String header(String name);

    default String parameter(String name) {
        return null;
    }

    default String remoteIp() {
        return null;
    }

    default String method() {
        return null;
    }

    default String path() {
        return null;
    }
}
