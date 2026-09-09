package com.iseekfree.common.sdk.web.mvc;

import com.iseekfree.common.sdk.common.auth.AuthIdentity;
import com.iseekfree.common.sdk.common.auth.AuthService;
import com.iseekfree.common.sdk.common.exception.ClawException;
import com.iseekfree.common.sdk.common.exception.UnauthorizedException;
import com.iseekfree.common.sdk.common.json.Jsons;
import com.iseekfree.common.sdk.common.web.Response;
import com.iseekfree.common.sdk.web.autoconfigure.ClawWebProperties;
import com.iseekfree.common.sdk.web.context.AuthRequired;
import com.iseekfree.common.sdk.web.context.WebContext;
import com.iseekfree.common.sdk.web.context.WebContextFactory;
import com.iseekfree.common.sdk.web.context.WebContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class WebAuthInterceptor implements HandlerInterceptor {

    public static final String REQ_START_TIME = "__claw_req_start__";

    private final ClawWebProperties properties;
    private final WebContextFactory contextFactory;
    private final AuthService authService;

    public WebAuthInterceptor(ClawWebProperties properties, WebContextFactory contextFactory, AuthService authService) {
        this.properties = properties;
        this.contextFactory = contextFactory;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        WebContextHolder.clear();
        request.setAttribute(REQ_START_TIME, System.currentTimeMillis());
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        WebContext context = contextFactory.create(request, response);
        if (!(handler instanceof HandlerMethod handlerMethod) || !properties.getAuth().isEnabled()) {
            return true;
        }
        AuthRequired required = findAuthRequired(handlerMethod);
        if (required == null && !properties.getAuth().isRequiredByDefault()) {
            return true;
        }
        return authenticate(context, request, response, required);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        WebContextHolder.clear();
    }

    private boolean authenticate(WebContext context, HttpServletRequest request, HttpServletResponse response, AuthRequired required) throws IOException {
        try {
            AuthIdentity identity = authService.authenticate(contextFactory.createAuthRequest(context, request));
            applyIdentity(context, identity);
            if (required != null && required.domains().length > 0) {
                String domain = identity.getDomain().orElse(context.getDomain());
                boolean allowed = Arrays.asList(required.domains()).contains(domain);
                if (!allowed) {
                    throw new UnauthorizedException("Invalid domain visit");
                }
            }
            if (required != null && required.perms().length > 0 && !authService.isPermitted(identity, required.perms())) {
                throw new UnauthorizedException("No permission");
            }
            return true;
        } catch (ClawException ex) {
            writeFailure(response, ex.getCode(), ex.getMessage());
            return false;
        } catch (RuntimeException ex) {
            writeFailure(response, UnauthorizedException.CODE, ex.getMessage());
            return false;
        }
    }

    private void applyIdentity(WebContext context, AuthIdentity identity) {
        context.setUid(identity.getUserId());
        identity.getDomain().ifPresent(context::setDomain);
        identity.getSessionId().ifPresent(context::setSession);
        identity.getExpiresAt().ifPresent(context::setExpiredAt);
    }

    private AuthRequired findAuthRequired(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        AuthRequired methodAnnotation = method.getAnnotation(AuthRequired.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return handlerMethod.getBeanType().getAnnotation(AuthRequired.class);
    }

    private void writeFailure(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(Jsons.toJson(Response.failure(code, msg == null ? "Unauthorized" : msg)));
    }
}
