package com.iseekfree.common.sdk.common.tool;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommonToolTest {

    @Test
    void computesMd5AsLowercaseHex() {
        assertEquals("e10adc3949ba59abbe56e057f20f883e", MD5Helper.getMD5("123456"));
        assertEquals(32, MD5Helper.getRandomMD5().length());
    }

    @Test
    void encodesAndDecodesBase64() {
        byte[] source = "Atlas基础工具".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(source, Base64Helper.decode(Base64Helper.encode(source)));
        assertArrayEquals(source, Base64Helper.decode("  " + Base64Helper.encode(source) + "\n"));
    }

    @Test
    void keepsPkcs7AesCompatibility() {
        String encrypted = AESHelper.aes128Encrypt("hello,世界", "cookie-secret", "AES/CBC/PKCS7Padding");
        assertEquals("hello,世界", AESHelper.aes128Decrypt(encrypted, "cookie-secret", "AES/CBC/PKCS7Padding"));
        assertEquals(encrypted, AESHelper.aes128Encrypt("hello,世界", "cookie-secret", "AES/CBC/PKCS5Padding"));
    }

    @Test
    void appendsPrimitiveValuesInNetworkByteOrder() {
        ByteAppender appender = new ByteAppender();
        appender.appendInt(0x01020304).prependShort((short) 0x0506).appendByte((byte) 0x07);
        assertArrayEquals(new byte[]{5, 6, 1, 2, 3, 4, 7}, appender.getBytes());
        assertThrows(IllegalArgumentException.class, () -> appender.append(new byte[2], 1, 2));
    }

    @Test
    void preservesCollectionAndStringUtilities() {
        assertEquals("a,b", ArrayUtil.joins(new String[]{"a", "b"}, ","));
        assertEquals(Arrays.asList(3, 2, 1), CollectionUtil.reverse(Arrays.asList(1, 2, 3)));
        assertEquals("value", StringUtil.trim(" value "));
    }
}
