package com.igot.cb.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SunbirdApiRespTest {

    @Test
    void testGettersAndSetters() {
        SunbirdApiResp resp = new SunbirdApiResp();

        String id = "api.id";
        String ver = "1.0";
        String ts = "2024-06-01T12:00:00Z";
        SunbirdApiRespParam params = new SunbirdApiRespParam();
        String responseCode = "OK";
        SunbirdApiRespResult result = new SunbirdApiRespResult();

        resp.setId(id);
        resp.setVer(ver);
        resp.setTs(ts);
        resp.setParams(params);
        resp.setResponseCode(responseCode);
        resp.setResult(result);

        assertEquals(id, resp.getId());
        assertEquals(ver, resp.getVer());
        assertEquals(ts, resp.getTs());
        assertEquals(params, resp.getParams());
        assertEquals(responseCode, resp.getResponseCode());
        assertEquals(result, resp.getResult());
    }

    @Test
    void testDefaultValues() {
        SunbirdApiResp resp = new SunbirdApiResp();

        assertNull(resp.getId());
        assertNull(resp.getVer());
        assertNull(resp.getTs());
        assertNull(resp.getParams());
        assertNull(resp.getResponseCode());
        assertNull(resp.getResult());
    }
}
