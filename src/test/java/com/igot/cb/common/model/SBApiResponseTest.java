package com.igot.cb.common.model;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SBApiResponseTest {

    @Test
    void testDefaultConstructor() {
        SBApiResponse resp = new SBApiResponse();
        assertNotNull(resp.getVer());
        assertNotNull(resp.getTs());
        assertNotNull(resp.getParams());
        assertNull(resp.getId());
        assertNull(resp.getResponseCode());
        assertNotNull(resp.getResult());
    }

    @Test
    void testConstructorWithId() {
        SBApiResponse resp = new SBApiResponse("test-id");
        assertEquals("test-id", resp.getId());
        assertNotNull(resp.getVer());
        assertNotNull(resp.getTs());
        assertNotNull(resp.getParams());
    }

    @Test
    void testSettersAndGetters() {
        SBApiResponse resp = new SBApiResponse();
        resp.setId("id1");
        resp.setVer("v2");
        resp.setTs("2024-01-01T00:00:00Z");
        SunbirdApiRespParam params = new SunbirdApiRespParam("pid");
        resp.setParams(params);
        resp.setResponseCode(HttpStatus.OK);

        assertEquals("id1", resp.getId());
        assertEquals("v2", resp.getVer());
        assertEquals("2024-01-01T00:00:00Z", resp.getTs());
        assertEquals(params, resp.getParams());
        assertEquals(HttpStatus.OK, resp.getResponseCode());
    }

    @Test
    void testResultMapMethods() {
        SBApiResponse resp = new SBApiResponse();
        resp.put("key1", "value1");
        assertTrue(resp.containsKey("key1"));
        assertEquals("value1", resp.get("key1"));

        Map<String, Object> map = new HashMap<>();
        map.put("key2", 123);
        resp.putAll(map);
        assertTrue(resp.containsKey("key2"));
        assertEquals(123, resp.get("key2"));

        Map<String, Object> newResult = new HashMap<>();
        newResult.put("k", "v");
        resp.setResult(newResult);
        assertEquals(newResult, resp.getResult());
        assertEquals("v", resp.get("k"));
    }
}
