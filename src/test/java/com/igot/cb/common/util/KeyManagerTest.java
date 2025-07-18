package com.igot.cb.common.util;

import com.igot.cb.common.model.KeyData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.*;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KeyManagerTest {

    @Test
    void testLoadPublicKey_InvalidFormat() {
        String invalidPem = "not-a-key";
        assertThrows(Exception.class, () -> KeyManager.loadPublicKey(invalidPem));
    }
}
