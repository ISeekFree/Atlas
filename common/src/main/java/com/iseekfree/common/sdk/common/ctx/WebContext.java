package com.iseekfree.common.sdk.common.ctx;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP transport context: the {@link AtlasContext} base plus the servlet
 * request/response handles.
 *
 * <p>An application subclasses it to add HTTP-facing fields and registers a
 * {@link WebContextProvider}, then injects the subclass directly into a
 * controller method. The transport-neutral fields and the shared loaders live on
 * {@link AtlasContext}, so HTTP, gRPC and WebSocket all read the same base type
 * instead of one transport inheriting another.</p>
 */
public class WebContext extends AtlasContext {

    private transient HttpServletRequest request;
    private transient HttpServletResponse response;

    public String getServerName() {
        if (request == null) {
            return "";
        }
        int serverPort = request.getServerPort();
        if (serverPort == 80 || serverPort == 443) {
            return request.getServerName();
        }
        return request.getServerName() + ":" + serverPort;
    }

    @Override
    public void copyTo(AtlasContext target) {
        super.copyTo(target);
        if (target instanceof WebContext web && target != this) {
            web.request = request;
            web.response = response;
        }
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
}
