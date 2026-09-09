package com.iseekfree.common.sdk.web.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface WebContextCustomizer {

    void customize(WebContext context, HttpServletRequest request, HttpServletResponse response);
}
