package com.iseekfree.common.sdk.common.exception;

import com.iseekfree.common.sdk.common.web.ErrorCodes;

/**
 * The unified framework exception: throw it from any request handler and the
 * SDK renders the {@code {code,msg,data}} envelope to the caller.
 *
 * <p>The business code is optional. {@code new AtlasException("boom")} uses
 * {@link ErrorCodes#SYSTEM_ERROR} ({@code -90}); pass an explicit code with
 * {@code new AtlasException(-1001, "boom")} when the caller should receive a
 * more specific one.</p>
 */
public class AtlasException extends RuntimeException {

    /** Code used when the caller does not pick one. */
    public static final int DEFAULT_CODE = ErrorCodes.SYSTEM_ERROR;

    private final int code;

    public AtlasException(String message) {
        this(DEFAULT_CODE, message);
    }

    public AtlasException(String message, Throwable cause) {
        this(DEFAULT_CODE, message, cause);
    }

    public AtlasException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AtlasException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
