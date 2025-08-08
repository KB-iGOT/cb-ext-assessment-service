package com.igot.cb.common.util;

import org.junit.jupiter.api.Test;

import java.security.*;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilTest {

    @Test
    void testVerifyRSASign_ValidSignature() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        String payload = "test payload";
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(pair.getPrivate());
        signature.update(payload.getBytes("US-ASCII"));
        byte[] sigBytes = signature.sign();

        boolean result = CryptoUtil.verifyRSASign(payload, sigBytes, pair.getPublic(), "SHA256withRSA");
        assertTrue(result);
    }

    @Test
    void testVerifyRSASign_InvalidSignature() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        String payload = "test payload";
        byte[] fakeSig = new byte[256]; // Invalid signature

        boolean result = CryptoUtil.verifyRSASign(payload, fakeSig, pair.getPublic(), "SHA256withRSA");
        assertFalse(result);
    }

    @Test
    void testVerifyRSASign_InvalidAlgorithm() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        String payload = "test payload";
        byte[] sig = new byte[256];

        boolean result = CryptoUtil.verifyRSASign(payload, sig, pair.getPublic(), "INVALID_ALGO");
        assertFalse(result);
    }
}
