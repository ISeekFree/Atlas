package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.common.ctx.AtlasContext;
import com.iseekfree.common.sdk.common.ctx.WebContextAuthorizer;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Business-owned permission check for the demo.
 *
 * <p>Atlas never interprets a permission itself; a {@code @AuthRequired(perms =
 * "demo:read")} is handed to this bean, which decides using whatever the demo's
 * context loader stored. Declaring no such bean makes any {@code perms} check
 * fail closed.</p>
 */
@Component
public class DemoWebContextAuthorizer implements WebContextAuthorizer {

    @Override
    public boolean isPermitted(AtlasContext context, String[] permissions) {
        Set<?> granted = context.getAttribute(DemoContextLoader.PERMISSIONS_ATTRIBUTE, Set.class).orElse(Set.of());
        for (String permission : permissions) {
            if (!granted.contains(permission)) {
                return false;
            }
        }
        return true;
    }
}
