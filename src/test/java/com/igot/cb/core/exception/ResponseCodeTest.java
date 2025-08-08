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

    @Test
    void testGetResponse_UnknownErrorCode() {
        // Should return null for unknown error code
        assertNull(ResponseCode.getResponse("UNKNOWN_CODE"));
    }

    @Test
    void testSettersAndGetters_AllEnums() {
        for (ResponseCode code : ResponseCode.values()) {
            code.setErrorCode("ERR_" + code.name());
            code.setErrorMessage("MSG_" + code.name());
            code.setResponseCode(999);

            assertEquals("ERR_" + code.name(), code.getErrorCode());
            assertEquals("MSG_" + code.name(), code.getErrorMessage());
            assertEquals(999, code.getResponseCode());
        }
    }
}
