package com.iseekfree.common.sdk.common.tool;

import java.util.Base64;

public final class Base64Helper {

    private Base64Helper() {
    }

    public static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decode(String value) {
        return Base64.getMimeDecoder().decode(value);
    }
}
