package com.igot.cb.common.util;

import org.junit.jupiter.api.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesCacheTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        // Reset the singleton instance before each test
        Field instance = PropertiesCache.class.getDeclaredField("propertiesCache");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void testSingletonInstance() {
        PropertiesCache cache1 = PropertiesCache.getInstance();
        PropertiesCache cache2 = PropertiesCache.getInstance();
        assertSame(cache1, cache2);
    }

    @Test
    void testSaveAndGetConfigProperty() {
        PropertiesCache cache = PropertiesCache.getInstance();
        cache.saveConfigProperty("testKey", "testValue");
        assertEquals("testValue", cache.getProperty("testKey"));
    }

    @Test
    void testGetPropertyReturnsKeyIfNotFound() {
        PropertiesCache cache = PropertiesCache.getInstance();
        assertEquals("unknownKey", cache.getProperty("unknownKey"));
    }

    @Test
    void testReadProperty() {
        PropertiesCache cache = PropertiesCache.getInstance();
        cache.saveConfigProperty("readKey", "readValue");
        assertEquals("readValue", cache.readProperty("readKey"));
    }

    @Test
    void testReadCustomError() {
        PropertiesCache cache = PropertiesCache.getInstance();
        cache.saveConfigProperty("CUSTOM_ERROR", "errorValue");
        assertEquals("errorValue", cache.readCustomError("CUSTOM ERROR"));
    }
}
