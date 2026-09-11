package com.iseekfree.common.sdk.grpc.client.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties("framework.grpc.client")
public class AtlasGrpcClientProperties {

    private boolean enabled = true;
    private final Map<String, Channel> channels = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    public static class Channel {
        private String target;
        private boolean plaintext = true;
        private Integer maxInboundMessageSize;

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public boolean isPlaintext() {
            return plaintext;
        }

        public void setPlaintext(boolean plaintext) {
            this.plaintext = plaintext;
        }

        public Integer getMaxInboundMessageSize() {
            return maxInboundMessageSize;
        }

        public void setMaxInboundMessageSize(Integer maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
        }
    }
}
