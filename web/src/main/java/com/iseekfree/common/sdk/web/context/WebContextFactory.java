package com.iseekfree.common.sdk.web.context;

import com.iseekfree.common.sdk.common.auth.AuthRequest;
import com.iseekfree.common.sdk.common.auth.CookieStyleAuthService;
import com.iseekfree.common.sdk.web.autoconfigure.ClawWebProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

public class WebContextFactory {

    private final ClawWebProperties properties;
    private final List<WebContextCustomizer> customizers;

    public WebContextFactory(ClawWebProperties properties, List<WebContextCustomizer> customizers) {
        this.properties = properties;
        this.customizers = List.copyOf(customizers);
    }

    public WebContext create(HttpServletRequest request, HttpServletResponse response) {
        return WebContextHolder.current().orElseGet(() -> {
            WebContext context = new WebContext();
            context.setRequest(request);
            context.setResponse(response);
            context.setAppTag(header(request, "appTag"));
            context.setOs(header(request, "os"));
            context.setOsVersion(header(request, "osv"));
            context.setAppVersion(header(request, "av"));
            context.setPackageName(header(request, "packageName"));
            context.setDeviceId(firstNonBlank(header(request, "deviceId"), header(request, "udid")));
            context.setIp(resolveIp(request));
            context.setSimplyArgs(simpleArgs(request));
            normalizeLocale(context, request);
            previewTokenClaims(context, request);
            customizers.forEach(customizer -> customizer.customize(context, request, response));
            WebContextHolder.set(context);
            return context;
        });
    }

    public AuthRequest createAuthRequest(WebContext context, HttpServletRequest request) {
        AuthToken token = resolveAuthToken(request);
        AuthRequest.Builder builder = AuthRequest.builder()
                .domain(context.getDomain())
                .appTag(context.getAppTag())
                .ip(context.getIp())
                .attribute("deviceId", context.getDeviceId());
        if (token != null) {
            builder.token(token.value()).admin(token.admin());
        }
        return builder.build();
    }

    private void previewTokenClaims(WebContext context, HttpServletRequest request) {
        AuthToken token = resolveAuthToken(request);
        if (token != null) {
            fillFromToken(context, token.value());
        }
    }

    private AuthToken resolveAuthToken(HttpServletRequest request) {
        for (String header : properties.getAuth().getAdminTokenHeaders()) {
            String token = header(request, header);
            if (token != null && !token.isBlank()) {
                return new AuthToken(token, true);
            }
        }
        for (String header : properties.getAuth().getTokenHeaders()) {
            String token = header(request, header);
            if (token != null && !token.isBlank()) {
                return new AuthToken(token, false);
            }
        }
        return null;
    }

    private void fillFromToken(WebContext context, String token) {
        Map<String, String> values = CookieStyleAuthService.parse(CookieStyleAuthService.stripBearer(token));
        context.setUid(firstNonBlank(values.get(CookieStyleAuthService.UID), values.get("uid"), values.get("userId"), values.get("sub")));
        context.setDomain(firstNonBlank(values.get(CookieStyleAuthService.DOMAIN), values.get("domain")));
        context.setSession(firstNonBlank(values.get(CookieStyleAuthService.SESSION), values.get("session"), values.get("sid")));
        String expires = firstNonBlank(values.get(CookieStyleAuthService.EXPIRES_AT), values.get("exp"), values.get("expiresAt"));
        if (expires != null) {
            try {
                long raw = Long.parseLong(expires);
                context.setExpiredAt(Instant.ofEpochMilli(raw < 10_000_000_000L ? raw * 1000L : raw));
            } catch (NumberFormatException ignored) {
                // Token preview is best effort; AuthService performs authoritative validation.
            }
        }
    }

    private void normalizeLocale(WebContext context, HttpServletRequest request) {
        String header = header(request, "Accept-Language");
        Locale locale = request.getLocale();
        String tag = header != null && !header.isBlank() ? header.split(",")[0].trim() : locale.toLanguageTag();
        String normalized = tag.replace('-', '_');
        String[] parts = normalized.split("_");
        context.setLanguage(parts.length > 0 ? parts[0].toLowerCase(Locale.ROOT) : "en");
        context.setArea(parts.length > 1 ? parts[1].toLowerCase(Locale.ROOT) : "");
    }

    private static String resolveIp(HttpServletRequest request) {
        String ip = firstNonBlank(
                header(request, "x-forwarded-for"),
                header(request, "x-real-ip"),
                header(request, "Proxy-Client-IP"),
                header(request, "WL-Proxy-Client-IP"),
                request.getRemoteAddr()
        );
        if (ip != null && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }
        return ip;
    }

    private static String simpleArgs(HttpServletRequest request) {
        StringJoiner joiner = new StringJoiner("&");
        request.getParameterMap().forEach((key, values) -> {
            String value = values == null || values.length == 0 ? "" : values[0];
            joiner.add(key + "=" + value);
        });
        return joiner.toString();
    }

    private static String header(HttpServletRequest request, String name) {
        return request.getHeader(name);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private record AuthToken(String value, boolean admin) {
    }
}
