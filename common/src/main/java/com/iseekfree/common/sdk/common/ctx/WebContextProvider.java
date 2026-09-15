package com.iseekfree.common.sdk.common.ctx;

/**
 * Creates the context instance for a request. Return an application subclass to
 * make {@code NexusWebContext} (or any other type) injectable into controllers.
 */
@FunctionalInterface
public interface WebContextProvider {

    WebContext create(WebContextRequest request);
}
