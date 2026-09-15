package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.common.auth.JwtCodec;
import com.iseekfree.common.sdk.common.ctx.AtlasContext;
import com.iseekfree.common.sdk.common.ctx.TokenResolver;
import com.iseekfree.common.sdk.common.ctx.WebContextLoader;
import com.iseekfree.common.sdk.common.ctx.WebContextRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The demo's own token contract, shared by HTTP, gRPC and WebSocket.
 *
 * <p>These claim names ({@code uid}, {@code domain}, {@code session},
 * {@code perms}) belong to the demo, not to Atlas. Swapping them only requires
 * changing this loader; the SDK verifies the signature and never reads a
 * business claim.</p>
 */
@Component
public class DemoContextLoader implements WebContextLoader {

    public static final String PERMISSIONS_ATTRIBUTE = "demoPermissions";
    public static final String ADMIN_ATTRIBUTE = "demoAdmin";

    private final JwtCodec jwtCodec;

    public DemoContextLoader(ObjectProvider<JwtCodec> jwtCodec) {
        this.jwtCodec = jwtCodec.getIfAvailable();
    }

    @Override
    public void load(AtlasContext context, WebContextRequest request) {
        TokenResolver.Token token = TokenResolver.resolve(request);
        if (jwtCodec == null || token == null || !token.isPresent()) {
            return;
        }
        jwtCodec.tryVerify(token.value()).ifPresent(claims -> apply(context, claims, token.admin()));
    }

    private void apply(AtlasContext context, Map<String, Object> claims, boolean admin) {
        String uid = text(claims.get("uid"));
        if (uid == null) {
            return;
        }
        context.setUid(uid);
        context.setAttribute(ADMIN_ATTRIBUTE, admin || booleanValue(claims.get("admin")));
        text(claims.get("domain"), context::setDomain);
        text(claims.get("session"), context::setSession);
        Object expires = claims.get("exp");
        if (expires instanceof Number number) {
            context.setExpiredAt(Instant.ofEpochSecond(number.longValue()));
        }
        context.setAttribute(PERMISSIONS_ATTRIBUTE, permissions(claims.get("perms")));
    }

    private static Set<String> permissions(Object value) {
        Set<String> permissions = new LinkedHashSet<>();
        String text = text(value);
        if (text == null) {
            return permissions;
        }
        for (String permission : text.split("[,|]")) {
            String trimmed = permission.trim();
            if (!trimmed.isEmpty()) {
                permissions.add(trimmed);
            }
        }
        return permissions;
    }

    private static boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    private static void text(Object value, java.util.function.Consumer<String> consumer) {
        String text = text(value);
        if (text != null) {
            consumer.accept(text);
        }
    }

    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }
}
