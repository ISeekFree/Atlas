package com.iseekfree.common.sdk.web.mvc;

import com.iseekfree.common.sdk.common.web.Response;
import org.springframework.http.HttpStatus;

/**
 * A framework-resolved failure: the unified {@code {code,msg,data}} envelope
 * plus the HTTP status to send it with.
 *
 * <p>Most failures keep {@code HTTP 200} and communicate the business error
 * through {@code code}; {@link #of(int, int, String)} lets a resolver preserve
 * an infrastructural status such as {@code 404} or {@code 503}.</p>
 */
public record ExceptionResponse(int httpStatus, int code, String msg) {

    /** Envelope-only failure, answered with {@code HTTP 200}. */
    public static ExceptionResponse of(int code, String msg) {
        return new ExceptionResponse(HttpStatus.OK.value(), code, msg);
    }

    /** Failure answered with an explicit HTTP status; {@code code} is the body code. */
    public static ExceptionResponse of(int httpStatus, int code, String msg) {
        return new ExceptionResponse(httpStatus, code, msg);
    }

    public Response<Void> body() {
        return Response.failure(code, msg);
    }
}
