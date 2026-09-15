package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.web.mvc.ExceptionResponse;
import com.iseekfree.common.sdk.web.mvc.ExceptionResponseResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Business-owned exception mapping for the demo.
 *
 * <p>The SDK already answers anything uncaught with the unified envelope
 * (and maps {@link IllegalArgumentException} to {@code 404}). This bean shows
 * how a business gives a specific exception its own code and message while
 * keeping the same {@code {code,msg,data}} shape; it runs before the SDK
 * default policy.</p>
 */
@Component
public class DemoExceptionResponseResolver implements ExceptionResponseResolver {

    @Override
    public ExceptionResponse resolve(Throwable error, HttpServletRequest request) {
        if (error instanceof IllegalArgumentException) {
            return ExceptionResponse.of(-91, "invalid argument: " + error.getMessage());
        }
        return null;
    }
}
