package com.iseekfree.common.sdk.common.assertion;

import com.iseekfree.common.sdk.common.exception.AtlasException;

/**
 * Framework-level precondition assertions for expected business failures.
 *
 * <p>Unlike Java's {@link java.util.Objects} helpers, these assertions always
 * raise {@link AtlasException}. The web and gRPC adapters can therefore turn
 * a failed request precondition into the SDK's normal business response
 * without logging it as an unhandled system exception.</p>
 */
public final class AtlasAssert {

    private AtlasAssert() {
    }

    /**
     * Requires a condition to be true, using the default system/business code.
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new AtlasException(message);
        }
    }

    /**
     * Requires a condition to be true with an explicit business code.
     */
    public static void isTrue(boolean condition, int code, String message) {
        if (!condition) {
            throw new AtlasException(code, message);
        }
    }

    /**
     * Requires a value to be non-null and returns it for inline validation.
     */
    public static <T> T notNull(T value, String message) {
        if (value == null) {
            throw new AtlasException(message);
        }
        return value;
    }

    /**
     * Requires a value to be non-null with an explicit business code.
     */
    public static <T> T notNull(T value, int code, String message) {
        if (value == null) {
            throw new AtlasException(code, message);
        }
        return value;
    }

    /**
     * Requires a string to contain at least one non-whitespace character and
     * returns the original value unchanged.
     */
    public static String notBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new AtlasException(message);
        }
        return value;
    }

    /**
     * Requires a non-blank string with an explicit business code.
     */
    public static String notBlank(String value, int code, String message) {
        if (value == null || value.isBlank()) {
            throw new AtlasException(code, message);
        }
        return value;
    }

    /**
     * Spring-style alias for {@link #notBlank(String, String)}.
     */
    public static String hasText(String value, String message) {
        return notBlank(value, message);
    }

    /**
     * Fails immediately with the default business code.
     */
    public static <T> T fail(String message) {
        throw new AtlasException(message);
    }

    /**
     * Fails immediately with an explicit business code.
     */
    public static <T> T fail(int code, String message) {
        throw new AtlasException(code, message);
    }
}
