package com.iseekfree.common.sdk.common.ctx;

import java.util.List;

/**
 * The single token-resolution entry point shared by HTTP, gRPC and WebSocket.
 * It only locates the raw token from configured header names; parsing/verifying
 * it stays in the application's {@link WebContextLoader}.
 */
public final class TokenResolver {

    public static final List<String> DEFAULT_TOKEN_HEADERS = List.of("token", "Authorization", "accessToken");
    public static final List<String> DEFAULT_ADMIN_TOKEN_HEADERS = List.of("adminToken");

    private TokenResolver() {
    }

    public record Token(String value, boolean admin) {

        public boolean isPresent() {
            return value != null && !value.isBlank();
        }
    }

    /** Resolves with the default header precedence. */
    public static Token resolve(WebContextRequest request) {
        return resolve(request, DEFAULT_ADMIN_TOKEN_HEADERS, DEFAULT_TOKEN_HEADERS);
    }

    /**
     * Admin headers are matched first so an operations call can be privileged;
     * the optional {@code Bearer } prefix is always stripped.
     */
    public static Token resolve(WebContextRequest request, List<String> adminHeaders, List<String> tokenHeaders) {
        if (request == null) {
            return null;
        }
        for (String header : adminHeaders) {
            String value = request.header(header);
            if (value != null && !value.isBlank()) {
                return new Token(stripBearer(value), true);
            }
        }
        for (String header : tokenHeaders) {
            String value = request.header(header);
            if (value != null && !value.isBlank()) {
                return new Token(stripBearer(value), false);
            }
        }
        return null;
    }

    /** Removes an optional case-insensitive {@code Bearer } prefix. */
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
}
