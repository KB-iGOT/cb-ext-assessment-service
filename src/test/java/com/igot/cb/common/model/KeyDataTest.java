package com.igot.cb.common.model;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

class KeyDataTest {

    @Test
    void testConstructorAndGetters() throws Exception {
        String keyId = "test-key-id";
        PublicKey publicKey = generateTestPublicKey();

        KeyData keyData = new KeyData(keyId, publicKey);

        assertEquals(keyId, keyData.getKeyId());
        assertEquals(publicKey, keyData.getPublicKey());
    }

    @Test
    void testSetters() throws Exception {
        KeyData keyData = new KeyData(null, null);

        String keyId = "new-key-id";
        PublicKey publicKey = generateTestPublicKey();

        keyData.setKeyId(keyId);
        keyData.setPublicKey(publicKey);

        assertEquals(keyId, keyData.getKeyId());
        assertEquals(publicKey, keyData.getPublicKey());
    }

    // Utility method to generate a dummy PublicKey for testing
    private PublicKey generateTestPublicKey() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        KeyPair pair = keyGen.generateKeyPair();
        return pair.getPublic();
    }
}
