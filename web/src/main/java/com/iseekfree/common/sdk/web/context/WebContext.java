package com.iseekfree.common.sdk.web.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class WebContext {

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
    private String packageName;
    private String deviceId;
    private String simplyArgs;
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private HttpServletRequest request;
    private HttpServletResponse response;

    public String getServerName() {
        if (request == null) {
            return "";
        }
        int serverPort = request.getServerPort();
        if (serverPort == 80 || serverPort == 443) {
            return request.getServerName();
        }
        return request.getServerName() + ":" + serverPort;
    }

    public boolean isIOS() {
        String osInfo = (nullToEmpty(os) + "/" + nullToEmpty(osVersion)).toLowerCase(Locale.ROOT);
        return osInfo.contains("iphone") || osInfo.contains("ipad") || osInfo.contains("ipod") || osInfo.contains("ios");
    }

    public boolean isAndroid() {
        String osInfo = (nullToEmpty(os) + "/" + nullToEmpty(osVersion)).toLowerCase(Locale.ROOT);
        return osInfo.contains("android");
    }

    public boolean isPC() {
        return !isIOS() && !isAndroid();
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
