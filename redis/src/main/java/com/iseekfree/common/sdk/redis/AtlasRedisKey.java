package com.iseekfree.common.sdk.redis;

public class AtlasRedisKey {

    private final AtlasRedisProperties properties;

    public AtlasRedisKey(AtlasRedisProperties properties) {
        this.properties = properties;
    }

    public String of(String key) {
        String prefix = properties.getKeyPrefix();
        if (prefix == null || prefix.isBlank()) {
            return key;
        }
        return prefix.endsWith(":") ? prefix + key : prefix + ":" + key;
    }
}
