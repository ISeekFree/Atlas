package com.iseekfree.common.sdk.web.mvc;

import com.iseekfree.common.sdk.common.exception.ClawException;
import com.iseekfree.common.sdk.common.web.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClawException.class)
    public Response<Void> handleClawException(ClawException ex) {
        return Response.failure(ex.getCode(), ex.getMessage());
    }
}
