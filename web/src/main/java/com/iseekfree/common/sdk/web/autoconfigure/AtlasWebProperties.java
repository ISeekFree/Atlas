package com.iseekfree.common.sdk.web.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("framework.web")
public class AtlasWebProperties {

    private boolean enabled = true;
    private final Response response = new Response();
    private final Cors cors = new Cors();
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

    public Cors getCors() {
        return cors;
    }

    public Auth getAuth() {
        return auth;
    }

    /**
     * Application-wide CORS policy, registered by the SDK as an early servlet
     * {@code CorsFilter} so preflight {@code OPTIONS} requests and error
     * responses are decorated before the application's own auth interceptor
     * runs. Business APIs no longer declare their own filter or MVC mapping.
     */
    public static class Cors {
        private boolean enabled = true;
        private String pathPattern = "/**";
        private final List<String> allowedOriginPatterns = new ArrayList<>(List.of("*"));
        private final List<String> allowedMethods = new ArrayList<>(
                List.of("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        private final List<String> allowedHeaders = new ArrayList<>(List.of("*"));
        private final List<String> exposedHeaders = new ArrayList<>(List.of("*"));
        private boolean allowCredentials = false;
        private long maxAge = 1800;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPathPattern() {
            return pathPattern;
        }

        public void setPathPattern(String pathPattern) {
            this.pathPattern = pathPattern;
        }

        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
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
        /** Headers carrying an end-user token; they resolve to {@code admin = false}. */
        private final List<String> tokenHeaders = new ArrayList<>(List.of("token", "Authorization", "accessToken"));
        /**
         * Headers carrying an operations/admin token; they are matched first and resolve to
         * {@code admin = true} so a custom {@code WebContextLoader} can grant elevated privileges.
         */
        private final List<String> adminTokenHeaders = new ArrayList<>(List.of("adminToken"));
        private final Jwt jwt = new Jwt();

        public List<String> getTokenHeaders() {
            return tokenHeaders;
        }

        public List<String> getAdminTokenHeaders() {
            return adminTokenHeaders;
        }

        public Jwt getJwt() {
            return jwt;
        }
    }

    /**
     * JWT crypto settings for the SDK's convenience {@code JwtCodec} bean. The
     * SDK only signs/verifies; claim names stay in the application's
     * {@code WebContextLoader}. Leave {@code secret} empty to skip that bean and
     * register your own {@code JwtCodec}.
     */
    public static class Jwt {
        private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }
}
