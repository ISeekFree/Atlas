package com.iseekfree.common.sdk.web.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("framework.web")
public class AtlasWebProperties {

    private boolean enabled = true;
    private final Response response = new Response();
    private final Auth auth = new Auth();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Response getResponse() {
        return response;
    }

    public Auth getAuth() {
        return auth;
    }

    public static class Response {
        private boolean wrap = true;
        private final List<String> notWrap = new ArrayList<>(List.of("/actuator"));

        public boolean isWrap() {
            return wrap;
        }

        public void setWrap(boolean wrap) {
            this.wrap = wrap;
        }

        public List<String> getNotWrap() {
            return notWrap;
        }
    }

    public static class Auth {
        private boolean enabled = true;
        private boolean requiredByDefault;
        private final List<String> tokenHeaders = new ArrayList<>(List.of("token", "Authorization", "accessToken"));
        private final List<String> adminTokenHeaders = new ArrayList<>(List.of("adminToken"));
        private final List<String> excludedPatterns = new ArrayList<>(List.of(
                "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/favicon.ico"
        ));

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isRequiredByDefault() {
            return requiredByDefault;
        }

        public void setRequiredByDefault(boolean requiredByDefault) {
            this.requiredByDefault = requiredByDefault;
        }

        public List<String> getTokenHeaders() {
            return tokenHeaders;
        }

        public List<String> getAdminTokenHeaders() {
            return adminTokenHeaders;
        }

        public List<String> getExcludedPatterns() {
            return excludedPatterns;
        }
    }
}
