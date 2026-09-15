package com.iseekfree.common.sdk.web.mvc;

import com.iseekfree.common.sdk.common.exception.AtlasException;
import com.iseekfree.common.sdk.common.web.ErrorCodes;
import com.iseekfree.common.sdk.common.web.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Converts uncaught MVC exceptions into the unified {@code {code,msg,data}}
 * envelope so a frontend never receives a framework default error page.
 *
 * <p>{@link AtlasException} keeps its own business code. Any other exception is
 * offered to the registered {@link ExceptionResponseResolver} beans first; the
 * SDK's own default policy then maps {@link IllegalArgumentException} to
 * {@code 404} and answers everything else with {@code code = -90}. A consuming
 * application can replace the policy by declaring its own
 * {@code @RestControllerAdvice} with a higher precedence, or by registering
 * resolver beans (which run before the SDK default).</p>
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final List<ExceptionResponseResolver> resolvers;

    public GlobalExceptionHandler() {
        this(List.of());
    }

    public GlobalExceptionHandler(List<ExceptionResponseResolver> resolvers) {
        this.resolvers = List.copyOf(resolvers);
    }

    @ExceptionHandler(AtlasException.class)
    public ResponseEntity<Response<Void>> handleAtlasException(AtlasException ex) {
        return ResponseEntity.ok(Response.failure(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        for (ExceptionResponseResolver resolver : resolvers) {
            ExceptionResponse resolved = resolver.resolve(ex, request);
            if (resolved != null) {
                return ResponseEntity.status(resolved.httpStatus()).body(resolved.body());
            }
        }
        ExceptionResponse fallback = defaultResponse(ex);
        return ResponseEntity.status(fallback.httpStatus()).body(fallback.body());
    }

    /**
     * SDK default policy when no business resolver handled the exception: an
     * unknown identifier surfaces as {@code 404}, everything else keeps the
     * unified {@code code = -90} envelope.
     */
    private static ExceptionResponse defaultResponse(Exception ex) {
        // if (ex instanceof IllegalArgumentException) {
        //     int status = HttpStatus.NOT_FOUND.value();
        //     return ExceptionResponse.of(status, status, messageOf(ex));
        // }
        return ExceptionResponse.of(ErrorCodes.SYSTEM_ERROR, messageOf(ex));
    }

    private static String messageOf(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? "Internal server error" : message;
    }
}
