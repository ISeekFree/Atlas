package com.iseekfree.common.sdk.common.auth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class AuthRequest {

    private final String token;
    private final String domain;
    private final String appTag;
    private final String ip;
    private final boolean admin;
    private final Map<String, Object> attributes;

    private AuthRequest(Builder builder) {
        this.token = builder.token;
        this.domain = builder.domain;
        this.appTag = builder.appTag;
        this.ip = builder.ip;
        this.admin = builder.admin;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(builder.attributes));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    public Optional<String> getDomain() {
        return Optional.ofNullable(domain);
    }

    public Optional<String> getAppTag() {
        return Optional.ofNullable(appTag);
    }

    public Optional<String> getIp() {
        return Optional.ofNullable(ip);
    }

    public boolean isAdmin() {
        return admin;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static final class Builder {
        private String token;
        private String domain;
        private String appTag;
        private String ip;
        private boolean admin;
        private final Map<String, Object> attributes = new LinkedHashMap<>();

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder appTag(String appTag) {
            this.appTag = appTag;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder admin(boolean admin) {
            this.admin = admin;
            return this;
        }

        public Builder attribute(String key, Object value) {
            if (key != null && !key.isBlank() && value != null) {
                this.attributes.put(key, value);
            }
            return this;
        }

        public AuthRequest build() {
            return new AuthRequest(this);
        }
    }
}
