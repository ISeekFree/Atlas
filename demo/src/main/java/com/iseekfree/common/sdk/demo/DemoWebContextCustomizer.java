package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.common.ctx.WebContext;
import com.iseekfree.common.sdk.web.context.WebContextCustomizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class DemoWebContextCustomizer implements WebContextCustomizer {

    @Override
    public void customize(WebContext context, HttpServletRequest request, HttpServletResponse response) {
        String traceId = request.getHeader("x-demo-trace-id");
        if (traceId != null && !traceId.isBlank()) {
            context.setAttribute("demoTraceId", traceId);
        }
    }
}
