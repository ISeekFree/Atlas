package com.iseekfree.common.sdk.grpc.server.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("claw.grpc.server")
public class ClawGrpcServerProperties {

    private boolean enabled = true;
    private int port = 9090;
    private boolean reflectionEnabled = true;
    private boolean healthEnabled = true;
    private final Auth auth = new Auth();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isReflectionEnabled() {
        return reflectionEnabled;
    }

    public void setReflectionEnabled(boolean reflectionEnabled) {
        this.reflectionEnabled = reflectionEnabled;
    }

    public boolean isHealthEnabled() {
        return healthEnabled;
    }

    public void setHealthEnabled(boolean healthEnabled) {
        this.healthEnabled = healthEnabled;
    }

    public Auth getAuth() {
        return auth;
    }

    public static class Auth {
        private boolean enabled = true;
        private boolean required;
        private String innerToken;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getInnerToken() {
            return innerToken;
        }

        public void setInnerToken(String innerToken) {
            this.innerToken = innerToken;
        }
    }
}
