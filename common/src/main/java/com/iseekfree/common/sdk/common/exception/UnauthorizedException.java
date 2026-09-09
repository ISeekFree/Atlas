package com.iseekfree.common.sdk.common.exception;

public class UnauthorizedException extends ClawException {

    public static final int CODE = -94;

    public UnauthorizedException(String message) {
        super(CODE, message);
    }
}
