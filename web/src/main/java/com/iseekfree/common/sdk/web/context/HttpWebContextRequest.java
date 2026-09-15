package com.iseekfree.common.sdk.web.context;

import com.iseekfree.common.sdk.common.ctx.WebContextRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Adapts a servlet {@link HttpServletRequest} onto the transport-neutral
 * {@link WebContextRequest}, so the same {@code WebContextLoader} parses tokens
 * for HTTP, gRPC and WebSocket.
 */
public class HttpWebContextRequest implements WebContextRequest {

    private final HttpServletRequest request;

    public HttpWebContextRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String header(String name) {
        return request == null ? null : request.getHeader(name);
    }

    @Override
    public String parameter(String name) {
        return request == null ? null : request.getParameter(name);
    }

    @Override
    public String remoteIp() {
        if (request == null) {
            return null;
        }
        String ip = firstNonBlank(
                request.getHeader("x-forwarded-for"),
                request.getHeader("x-real-ip"),
                request.getHeader("Proxy-Client-IP"),
                request.getHeader("WL-Proxy-Client-IP"),
                request.getRemoteAddr()
        );
        if (ip != null && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public String method() {
        return request == null ? null : request.getMethod();
    }

    @Override
    public String path() {
        return request == null ? null : request.getRequestURI();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
