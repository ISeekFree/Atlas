package com.iseekfree.common.sdk.common.exception;

public class ClawException extends RuntimeException {

    private final int code;

    public ClawException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ClawException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
