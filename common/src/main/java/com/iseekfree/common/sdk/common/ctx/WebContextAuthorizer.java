package com.iseekfree.common.sdk.common.ctx;

/**
 * Business-owned permission check used by {@code @AuthRequired(perms = ...)}.
 * When no bean is registered, a context that must satisfy permissions is
 * rejected (fail-closed).
 */
@FunctionalInterface
public interface WebContextAuthorizer {

    boolean isPermitted(AtlasContext context, String[] permissions);
}
