package com.iseekfree.common.sdk.common.exception;

import com.iseekfree.common.sdk.common.web.ErrorCodes;

public class UnauthorizedException extends AtlasException {

    public static final int CODE = ErrorCodes.UNAUTHORIZED;

    public UnauthorizedException(String message) {
        super(CODE, message);
    }
}
