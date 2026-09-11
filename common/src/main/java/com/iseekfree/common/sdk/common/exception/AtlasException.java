package com.iseekfree.common.sdk.common.exception;

public class AtlasException extends RuntimeException {

    private final int code;

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
