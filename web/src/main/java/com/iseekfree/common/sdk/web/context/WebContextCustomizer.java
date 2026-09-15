package com.iseekfree.common.sdk.web.context;

import com.iseekfree.common.sdk.common.ctx.WebContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface WebContextCustomizer {

    void customize(WebContext context, HttpServletRequest request, HttpServletResponse response);
}
