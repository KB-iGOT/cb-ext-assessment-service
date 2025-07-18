package com.igot.cb.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base64UtilTest {

    @Test
    void testEncodeDecode_Default() {
        String original = "hello world";
        String encoded = Base64Util.encodeToString(original.getBytes(), Base64Util.DEFAULT);
        byte[] decoded = Base64Util.decode(encoded, Base64Util.DEFAULT);
        assertEquals(original, new String(decoded));
    }

    @Test
    void testEncodeDecode_NoPadding() {
        String original = "test";
        String encoded = Base64Util.encodeToString(original.getBytes(), Base64Util.NO_PADDING);
        // Should not end with '='
        assertFalse(encoded.endsWith("="));
        byte[] decoded = Base64Util.decode(encoded, Base64Util.NO_PADDING);
        assertEquals(original, new String(decoded));
    }

    @Test
    void testEncodeDecode_NoWrap() {
        String original = "this is a longer string to test no wrap option in base64 encoding";
        String encoded = Base64Util.encodeToString(original.getBytes(), Base64Util.NO_WRAP);
        assertFalse(encoded.contains("\n"));
        byte[] decoded = Base64Util.decode(encoded, Base64Util.NO_WRAP);
        assertEquals(original, new String(decoded));
    }

    @Test
    void testEncodeDecode_UrlSafe() {
        String original = "foo?bar=baz+qux/=";
        String encoded = Base64Util.encodeToString(original.getBytes(), Base64Util.URL_SAFE);
        assertFalse(encoded.contains("+"));
        assertFalse(encoded.contains("/"));
        byte[] decoded = Base64Util.decode(encoded, Base64Util.URL_SAFE);
        assertEquals(original, new String(decoded));
    }

    @Test
    void testEncodeDecode_Empty() {
        String original = "";
        String encoded = Base64Util.encodeToString(original.getBytes(), Base64Util.DEFAULT);
        assertEquals("", encoded);
        byte[] decoded = Base64Util.decode(encoded, Base64Util.DEFAULT);
        assertEquals(original, new String(decoded));
    }

    @Test
    void testDecode_InvalidInput() {
        String invalid = "!!!notbase64!!!";
        assertThrows(IllegalArgumentException.class, () -> Base64Util.decode(invalid, Base64Util.DEFAULT));
    }
}