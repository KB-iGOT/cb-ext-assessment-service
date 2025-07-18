package com.igot.cb.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SunbirdApiRespParamTest {

    @Test
    void testGettersAndSetters() {
        SunbirdApiRespParam param = new SunbirdApiRespParam();

        String resmsgid = "resmsgid1";
        String msgid = "msgid1";
        String err = "error";
        String status = "SUCCESS";
        String errmsg = "No error";

        param.setResmsgid(resmsgid);
        param.setMsgid(msgid);
        param.setErr(err);
        param.setStatus(status);
        param.setErrmsg(errmsg);

        assertEquals(resmsgid, param.getResmsgid());
        assertEquals(msgid, param.getMsgid());
        assertEquals(err, param.getErr());
        assertEquals(status, param.getStatus());
        assertEquals(errmsg, param.getErrmsg());
    }

    @Test
    void testDefaultConstructor() {
        SunbirdApiRespParam param = new SunbirdApiRespParam();

        assertNull(param.getResmsgid());
        assertNull(param.getMsgid());
        assertNull(param.getErr());
        assertNull(param.getStatus());
        assertNull(param.getErrmsg());
    }

    @Test
    void testConstructorWithId() {
        String id = "test-id";
        SunbirdApiRespParam param = new SunbirdApiRespParam(id);

        assertEquals(id, param.getResmsgid());
        assertEquals(id, param.getMsgid());
        assertNull(param.getErr());
        assertNull(param.getStatus());
        assertNull(param.getErrmsg());
    }
}
