package com.igot.cb.core.exception;

import com.igot.cb.common.util.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseCodeTest {

    @Test
    void testEnumValues() {
        assertEquals("UNAUTHORIZED_USER", ResponseCode.unAuthorized.getErrorCode());
        assertEquals("You are not authorized.", ResponseCode.unAuthorized.getErrorMessage());
        assertEquals("INTERNAL_ERROR", ResponseCode.internalError.getErrorCode());
        assertEquals("Process failed,please try again later.", ResponseCode.internalError.getErrorMessage());
        assertEquals(200, ResponseCode.OK.getResponseCode());
        assertEquals(400, ResponseCode.CLIENT_ERROR.getResponseCode());
        assertEquals(500, ResponseCode.SERVER_ERROR.getResponseCode());
    }

    @Test
    void testSettersAndGetters() {
        ResponseCode code = ResponseCode.unAuthorized;
        code.setErrorCode("ERR");
        code.setErrorMessage("msg");
        code.setResponseCode(401);

        assertEquals("ERR", code.getErrorCode());
        assertEquals("msg", code.getErrorMessage());
        assertEquals(401, code.getResponseCode());
    }

    @Test
    void testGetResponse_NullOrBlank() {
        assertNull(ResponseCode.getResponse(null));
        assertNull(ResponseCode.getResponse(""));
        assertNull(ResponseCode.getResponse("   "));
    }

    @Test
    void testGetResponse_Unauthorized() {
        assertEquals(ResponseCode.unAuthorized, ResponseCode.getResponse(Constants.UNAUTHORIZED));
    }

    @Test
    void testGetMessageAlwaysReturnsEmpty() {
        assertEquals("", ResponseCode.OK.getMessage(200));
        assertEquals("", ResponseCode.unAuthorized.getMessage(401));
    }
}
