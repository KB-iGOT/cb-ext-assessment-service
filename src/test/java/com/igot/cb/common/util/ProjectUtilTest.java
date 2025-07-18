package com.igot.cb.common.util;

import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.model.SunbirdApiRespParam;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ProjectUtilTest {

    @Test
    void testCreateDefaultResponse() {
        String api = "testApi";
        SBApiResponse response = ProjectUtil.createDefaultResponse(api);

        assertNotNull(response);
        assertEquals(api, response.getId());
        assertEquals(Constants.API_VERSION_1, response.getVer());
        assertNotNull(response.getParams());
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertNotNull(response.getTs());
    }

    @Test
    void testUpdateErrorDetails() {
        SBApiResponse response = new SBApiResponse();
        response.setParams(new SunbirdApiRespParam("id"));
        String errMsg = "Some error";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ProjectUtil.updateErrorDetails(response, errMsg, status);

        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(errMsg, response.getParams().getErrmsg());
        assertEquals(status, response.getResponseCode());
    }
}
