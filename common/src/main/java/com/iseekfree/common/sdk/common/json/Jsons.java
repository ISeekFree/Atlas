package com.iseekfree.common.sdk.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.iseekfree.common.sdk.common.exception.AtlasException;

public final class Jsons {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Jsons() {
    }

    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new AtlasException(500, "JSON serialization failed", ex);
        }
    }

    public static <T> T fromJson(String value, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(value, type);
        } catch (JsonProcessingException ex) {
            throw new AtlasException(500, "JSON deserialization failed", ex);
        }
    }
}
