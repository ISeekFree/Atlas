package com.iseekfree.common.sdk.common.tool;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;

public final class AESHelper {

    public static final String KEY_ALGORITHM = "AES";

    private static final IvParameterSpec DEFAULT_IV = new IvParameterSpec(new byte[16]);

    private AESHelper() {
    }

    public static byte[] init128key(String value) throws Exception {
        return MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
    }

    public static Key toKey(byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
        return secretKey;
    }

    public static byte[] encrypt(byte[] data, byte[] key, String algorithm) throws Exception {
        return encrypt(data, key, algorithm, DEFAULT_IV);
    }

    public static byte[] decrypt(byte[] data, byte[] key, String algorithm) throws Exception {
        return decrypt(data, key, algorithm, DEFAULT_IV);
    }

    public static byte[] encrypt(byte[] data, byte[] key, String algorithm, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(normalizeAlgorithm(algorithm));
        cipher.init(Cipher.ENCRYPT_MODE, toKey(key), iv);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data, byte[] key, String algorithm, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(normalizeAlgorithm(algorithm));
        cipher.init(Cipher.DECRYPT_MODE, toKey(key), iv);
        return cipher.doFinal(data);
    }

    public static String aes128Encrypt(String info, String key, String algorithm) {
        return aes128Encrypt(info, key, algorithm, DEFAULT_IV);
    }

    public static String aes128Decrypt(String encryptedInfo, String key, String algorithm) {
        return aes128Decrypt(encryptedInfo, key, algorithm, DEFAULT_IV);
    }

    public static String aes128Encrypt(String info, String key, String algorithm, IvParameterSpec iv) {
        try {
            byte[] encrypted = encrypt(info.getBytes(StandardCharsets.UTF_8), init128key(key), algorithm, iv);
            return Base64Helper.encode(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("AES encryption failed", e);
        }
    }

    public static String aes128Decrypt(String encryptedInfo, String key, String algorithm, IvParameterSpec iv) {
        try {
            byte[] decrypted = decrypt(Base64Helper.decode(encryptedInfo), init128key(key), algorithm, iv);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("AES decryption failed", e);
        }
    }

    private static String normalizeAlgorithm(String algorithm) {
        return algorithm.replace("PKCS7Padding", "PKCS5Padding");
    }
}
