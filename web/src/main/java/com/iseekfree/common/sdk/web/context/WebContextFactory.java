package com.iseekfree.common.sdk.web.context;

import com.iseekfree.common.sdk.common.ctx.TokenResolver;
import com.iseekfree.common.sdk.common.ctx.AtlasContext;
import com.iseekfree.common.sdk.common.ctx.WebContext;
import com.iseekfree.common.sdk.common.ctx.WebContextHolder;
import com.iseekfree.common.sdk.common.ctx.WebContextLoader;
import com.iseekfree.common.sdk.common.ctx.WebContextProvider;
import com.iseekfree.common.sdk.web.autoconfigure.AtlasWebProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * Builds the per-request {@link WebContext} for HTTP traffic.
 *
 * <p>The application supplies a {@link WebContextProvider} to decide which
 * concrete context type is created (its own subclass or the base type) and one
 * or more {@link WebContextLoader} beans to fill token-derived fields. The
 * factory itself only populates transport basics (ip, locale, app/os headers,
 * ...), so nothing auth-specific — or business-specific such as a device id or
 * admin flag — is hard-coded here.</p>
 */
public class WebContextFactory {

    private final AtlasWebProperties properties;
    private final WebContextProvider provider;
    private final List<WebContextLoader> loaders;
    private final List<WebContextCustomizer> customizers;

    public WebContextFactory(AtlasWebProperties properties, WebContextProvider provider, List<WebContextLoader> loaders,
                             List<WebContextCustomizer> customizers) {
        this.properties = properties;
        this.provider = provider;
        this.loaders = List.copyOf(loaders);
        this.customizers = List.copyOf(customizers);
    }

    public WebContext create(HttpServletRequest request, HttpServletResponse response) {
        AtlasContext existing = WebContextHolder.currentOrNull();
        if (existing instanceof WebContext web) {
            return web;
        }
        HttpWebContextRequest contextRequest = new HttpWebContextRequest(request);
        WebContext created = provider == null ? new WebContext() : provider.create(contextRequest);
        if (created == null) {
            created = new WebContext();
        }
        final WebContext context = created;
        context.setRequest(request);
        context.setResponse(response);
        context.setAppTag(header(request, "appTag"));
        context.setOs(header(request, "os"));
        context.setOsVersion(header(request, "osv"));
        context.setAppVersion(header(request, "av"));
        context.setIp(contextRequest.remoteIp());
        context.setSimplyArgs(simpleArgs(request));
        normalizeLocale(context, request);
        for (WebContextLoader loader : loaders) {
            loader.load(context, contextRequest);
        }
        customizers.forEach(customizer -> customizer.customize(context, request, response));
        WebContextHolder.set(context);
        return context;
    }

    /** Resolves the raw token using the configured header precedence. */
    public TokenResolver.Token resolveToken(HttpServletRequest request) {
        return TokenResolver.resolve(
                new HttpWebContextRequest(request),
                properties.getAuth().getAdminTokenHeaders(),
                properties.getAuth().getTokenHeaders()
        );
    }

    public void clear() {
        WebContextHolder.clear();
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

}
