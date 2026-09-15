package com.iseekfree.common.sdk.common.ctx;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Transport-neutral per-call state shared by HTTP, gRPC and WebSocket.
 *
 * <p>This is the common supertype of the three transport contexts:
 * {@link WebContext} (HTTP), {@code com.iseekfree.common.sdk.grpc.common.GrpcContext}
 * (gRPC) and {@code com.iseekfree.common.sdk.web.context.WebsocketContext}
 * (WebSocket). It deliberately carries no transport handle — HTTP request/response
 * live on {@link WebContext} — so the shared loaders, authorizers and holders can
 * work against one type.</p>
 *
 * <p>Applications subclass it (or one of the transport subclasses) to add their
 * own fields (channel, package name, device id, permissions, ...), or store them
 * in the free-form {@link #getAttributes() attributes} map. Nothing here is
 * auth-specific; token parsing lives in a {@link WebContextLoader}.</p>
 */
public class AtlasContext {

    private String uid;
    private String ip;
    private String os;
    private String osVersion;
    private String language;
    private String area;
    private String domain;
    private String session;
    private Instant expiredAt;
    private String appVersion;
    private String appTag;
    private String simplyArgs;
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    public boolean isIOS() {
        String info = (nullToEmpty(os) + "/" + nullToEmpty(osVersion)).toLowerCase(Locale.ROOT);
        return info.contains("iphone") || info.contains("ipad") || info.contains("ipod") || info.contains("ios");
    }

    public boolean isAndroid() {
        return (nullToEmpty(os) + "/" + nullToEmpty(osVersion)).toLowerCase(Locale.ROOT).contains("android");
    }

    public boolean isPC() {
        return !isIOS() && !isAndroid();
    }

    /** Copies every base field into a sibling context instance. */
    public void copyTo(AtlasContext target) {
        if (target == null || target == this) {
            return;
        }
        target.uid = uid;
        target.ip = ip;
        target.os = os;
        target.osVersion = osVersion;
        target.language = language;
        target.area = area;
        target.domain = domain;
        target.session = session;
        target.expiredAt = expiredAt;
        target.appVersion = appVersion;
        target.appTag = appTag;
        target.simplyArgs = simplyArgs;
        target.attributes.putAll(attributes);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppTag() {
        return appTag;
    }

    public void setAppTag(String appTag) {
        this.appTag = appTag;
    }

    public String getSimplyArgs() {
        return simplyArgs;
    }

    public void setSimplyArgs(String simplyArgs) {
        this.simplyArgs = simplyArgs;
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    public <T> Optional<T> getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }

    public void setAttribute(String key, Object value) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (value == null) {
            attributes.remove(key);
            return;
        }
        attributes.put(key, value);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
