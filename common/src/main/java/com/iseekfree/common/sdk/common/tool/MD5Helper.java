package com.iseekfree.common.sdk.common.tool;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public final class MD5Helper {

    private MD5Helper() {
    }

    public static String getMD5(String message) {
        return getMD5(message.getBytes(StandardCharsets.UTF_8));
    }

    public static String getRandomMD5() {
        return getMD5(UUID.randomUUID().toString());
    }

    public static String getMD5(byte[] buffer) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(buffer);
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(Character.forDigit((value >>> 4) & 0x0f, 16));
                result.append(Character.forDigit(value & 0x0f, 16));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 is not available", e);
        }
    }
}
