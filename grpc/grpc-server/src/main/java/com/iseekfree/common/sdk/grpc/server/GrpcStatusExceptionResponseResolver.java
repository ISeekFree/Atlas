package com.iseekfree.common.sdk.grpc.server;

import com.iseekfree.common.sdk.common.web.ErrorCodes;
import com.iseekfree.common.sdk.web.mvc.ExceptionResponse;
import com.iseekfree.common.sdk.web.mvc.ExceptionResponseResolver;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

/**
 * Framework bridge from a failed downstream gRPC call to the HTTP contract of
 * the caller's own endpoint.
 *
 * <p>An inbound REST handler that calls another service through a gRPC stub
 * would otherwise surface {@link StatusRuntimeException} as a generic
 * {@code 500} envelope. This resolver maps the gRPC status onto the matching
 * HTTP status ({@code NOT_FOUND → 404}, {@code UNAVAILABLE/DEADLINE_EXCEEDED →
 * 503}, other downstream failures → {@code 502}) while keeping the unified
 * {@code {code,msg,data}} body. A downstream {@code UNAUTHENTICATED} is a login
 * failure, so it always answers with {@code code = -94} ({@code 401}) like the
 * SDK's HTTP auth interceptor. Register a business
 * {@link ExceptionResponseResolver} to override it.</p>
 */
public class GrpcStatusExceptionResponseResolver implements ExceptionResponseResolver {

    @Override
    public ExceptionResponse resolve(Throwable error, HttpServletRequest request) {
        if (!(error instanceof StatusRuntimeException grpc)) {
            return null;
        }
        Status.Code code = grpc.getStatus().getCode();
        if (code == Status.Code.UNAUTHENTICATED) {
            return ExceptionResponse.of(HttpStatus.UNAUTHORIZED.value(), ErrorCodes.UNAUTHORIZED,
                    describe(grpc, HttpStatus.UNAUTHORIZED));
        }
        int status = httpStatusOf(code);
        return ExceptionResponse.of(status, status, describe(grpc, HttpStatus.valueOf(status)));
    }

    private static String describe(StatusRuntimeException grpc, HttpStatus fallback) {
        String message = grpc.getStatus().getDescription();
        if (message == null || message.isBlank()) {
            return fallback.getReasonPhrase();
        }
        return message;
    }

    private static int httpStatusOf(Status.Code code) {
        return switch (code) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND.value();
            case UNAVAILABLE, DEADLINE_EXCEEDED -> HttpStatus.SERVICE_UNAVAILABLE.value();
            default -> HttpStatus.BAD_GATEWAY.value();
        };
    }
}
