package com.iseekfree.common.sdk.common.web;

/**
 * Business codes carried by {@link Response#getCode()}.
 *
 * <p>{@code 0} means success; failures use negative codes so they never collide
 * with an HTTP status. Applications may return any code they like; these are the
 * defaults the SDK itself produces.</p>
 */
public final class ErrorCodes {

    /** Successful call. */
    public static final int SUCCESS = 0;
    /** An exception escaped the application without a mapped business code. */
    public static final int SYSTEM_ERROR = -90;
    /** Authentication is missing, invalid or expired. */
    public static final int UNAUTHORIZED = -94;

    private ErrorCodes() {
    }
}
