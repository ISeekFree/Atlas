package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.common.ctx.AuthRequired;
import com.iseekfree.common.sdk.common.ctx.WebContext;
import com.iseekfree.common.sdk.common.ctx.WebContextAuthorizer;
import com.iseekfree.common.sdk.common.ctx.WebContextHolder;
import com.iseekfree.common.sdk.common.exception.UnauthorizedException;
import com.iseekfree.common.sdk.common.json.Jsons;
import com.iseekfree.common.sdk.common.web.Response;
import com.iseekfree.common.sdk.web.context.WebContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Demo-owned HTTP auth interceptor.
 *
 * <p>Atlas no longer ships a {@code WebAuthInterceptor}: authentication and
 * authorization policy belong to the application. This is the reference
 * implementation a consuming service copies into its own {@code core}. It first
 * asks the SDK's shared {@link WebContextFactory} to build (and store) the
 * {@link WebContext} — the same factory the controller argument resolver uses —
 * then enforces {@link AuthRequired}: {@code uid} must be present, the
 * {@code domain} must match, and {@code perms} are delegated to the optional
 * {@link WebContextAuthorizer} (absent ⇒ deny).</p>
 */
public class DemoWebAuthInterceptor implements HandlerInterceptor {

    private final WebContextFactory contextFactory;
    private final WebContextAuthorizer authorizer;
    private final boolean requiredByDefault;

    public DemoWebAuthInterceptor(WebContextFactory contextFactory, WebContextAuthorizer authorizer, boolean requiredByDefault) {
        this.contextFactory = contextFactory;
        this.authorizer = authorizer;
        this.requiredByDefault = requiredByDefault;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        WebContextHolder.clear();
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        WebContext context = contextFactory.create(request, response);
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        AuthRequired required = findAuthRequired(handlerMethod);
        if (required == null && !requiredByDefault) {
            return true;
        }
        return authorize(context, response, required);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        WebContextHolder.clear();
    }

    private boolean authorize(WebContext context, HttpServletResponse response, AuthRequired required) throws IOException {
        if (context.getUid() == null || context.getUid().isBlank()) {
            writeFailure(response, UnauthorizedException.CODE, "Unauthorized");
            return false;
        }
        if (required != null && required.domains().length > 0 && !Arrays.asList(required.domains()).contains(context.getDomain())) {
            writeFailure(response, UnauthorizedException.CODE, "Invalid domain visit");
            return false;
        }
        if (required != null && required.perms().length > 0
                && (authorizer == null || !authorizer.isPermitted(context, required.perms()))) {
            writeFailure(response, UnauthorizedException.CODE, "No permission");
            return false;
        }
        return true;
    }

    private AuthRequired findAuthRequired(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        AuthRequired methodAnnotation = method.getAnnotation(AuthRequired.class);
        return methodAnnotation != null ? methodAnnotation : handlerMethod.getBeanType().getAnnotation(AuthRequired.class);
    }

    private void writeFailure(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(Jsons.toJson(Response.failure(code, msg)));
    }
}
