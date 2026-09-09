package com.iseekfree.common.sdk.common.auth;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class AuthIdentity {

    private final String userId;
    private final String domain;
    private final String sessionId;
    private final Instant expiresAt;
    private final Set<String> permissions;
    private final Map<String, Object> claims;

    private AuthIdentity(Builder builder) {
        this.userId = builder.userId;
        this.domain = builder.domain;
        this.sessionId = builder.sessionId;
        this.expiresAt = builder.expiresAt;
        this.permissions = Collections.unmodifiableSet(new LinkedHashSet<>(builder.permissions));
        this.claims = Collections.unmodifiableMap(new LinkedHashMap<>(builder.claims));
    }

    public static Builder builder(String userId) {
        return new Builder(userId);
    }

    public String getUserId() {
        return userId;
    }

    public Optional<String> getDomain() {
        return Optional.ofNullable(domain);
    }

    public Optional<String> getSessionId() {
        return Optional.ofNullable(sessionId);
    }

    public Optional<Instant> getExpiresAt() {
        return Optional.ofNullable(expiresAt);
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public boolean hasAnyPermission(String[] required) {
        if (required == null || required.length == 0) {
            return true;
        }
        for (String permission : required) {
            if (permissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    public static final class Builder {
        private final String userId;
        private String domain;
        private String sessionId;
        private Instant expiresAt;
        private final Set<String> permissions = new LinkedHashSet<>();
        private final Map<String, Object> claims = new LinkedHashMap<>();

        private Builder(String userId) {
            this.userId = Objects.requireNonNull(userId, "userId");
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder permission(String permission) {
            if (permission != null && !permission.isBlank()) {
                this.permissions.add(permission);
            }
            return this;
        }

        public Builder permissions(Iterable<String> permissions) {
            if (permissions != null) {
                permissions.forEach(this::permission);
            }
            return this;
        }

        public Builder claim(String key, Object value) {
            if (key != null && !key.isBlank() && value != null) {
                this.claims.put(key, value);
            }
            return this;
        }

        public Builder claims(Map<String, ?> claims) {
            if (claims != null) {
                claims.forEach(this::claim);
            }
            return this;
        }

        public AuthIdentity build() {
            return new AuthIdentity(this);
        }
    }
}
