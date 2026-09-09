package com.iseekfree.common.sdk.common.auth;

import com.iseekfree.common.sdk.common.exception.UnauthorizedException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CookieStyleAuthService implements AuthService {

    public static final String UID = "_u_";
    public static final String DOMAIN = "_d_";
    public static final String SESSION = "_s_";
    public static final String EXPIRES_AT = "_exp_";
    public static final String PERMISSIONS = "_perms_";

    @Override
    public AuthIdentity authenticate(AuthRequest request) {
        String token = request.getToken().filter(value -> !value.isBlank())
                .orElseThrow(() -> new UnauthorizedException("Missing token"));
        Map<String, String> values = parse(stripBearer(token));
        String userId = firstNonBlank(values.get(UID), values.get("uid"), values.get("userId"), values.get("sub"));
        if (userId == null) {
            throw new UnauthorizedException("Invalid token");
        }

        AuthIdentity.Builder builder = AuthIdentity.builder(userId)
                .domain(firstNonBlank(values.get(DOMAIN), values.get("domain"), request.getDomain().orElse(null)))
                .sessionId(firstNonBlank(values.get(SESSION), values.get("session"), values.get("sid")))
                .claims(values);

        parseEpochMillis(firstNonBlank(values.get(EXPIRES_AT), values.get("exp"), values.get("expiresAt")))
                .ifPresent(builder::expiresAt);

        String permissions = firstNonBlank(values.get(PERMISSIONS), values.get("perms"), values.get("permissions"));
        if (permissions != null) {
            Arrays.stream(permissions.split("[,|]")).map(String::trim).forEach(builder::permission);
        }

        AuthIdentity identity = builder.build();
        if (identity.isExpired(Instant.now())) {
            throw new UnauthorizedException("Token expired");
        }
        return identity;
    }

    public static Map<String, String> parse(String token) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String part : token.split("[;&]")) {
            int index = part.indexOf('=');
            if (index <= 0) {
                continue;
            }
            String key = decode(part.substring(0, index).trim());
            String value = decode(part.substring(index + 1).trim());
            if (!key.isEmpty()) {
                values.put(key, value);
            }
        }
        return values;
    }

    public static String stripBearer(String token) {
        if (token == null) {
            return null;
        }
        String trimmed = token.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed.substring(7).trim();
        }
        return trimmed;
    }

    private static java.util.Optional<Instant> parseEpochMillis(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Optional.empty();
        }
        try {
            long raw = Long.parseLong(value);
            long epochMillis = raw < 10_000_000_000L ? raw * 1000L : raw;
            return java.util.Optional.of(Instant.ofEpochMilli(epochMillis));
        } catch (NumberFormatException ex) {
            return java.util.Optional.empty();
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
