package com.igot.cb.common.util;

import com.igot.cb.common.model.SunbirdApiResp;
import com.igot.cb.common.model.SunbirdApiRespResult;
import com.igot.cb.common.model.SunbirdApiResultResponse;
import com.igot.cb.core.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserUtilityServiceImplTest {

    @InjectMocks
    private UserUtilityServiceImpl userUtilityService;

    @Mock
    private CbExtAssessmentServerProperties props;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(props.getSbUrl()).thenReturn("http://mock-sb/");
        when(props.getUserSearchEndPoint()).thenReturn("user/search");
    }

    @Test
    void testValidateUser_ValidUser() {
        SunbirdApiResultResponse response = new SunbirdApiResultResponse();
        response.setCount(1);
        SunbirdApiRespResult result = new SunbirdApiRespResult();
        result.setResponse(response);
        SunbirdApiResp apiResp = new SunbirdApiResp();
        apiResp.setResponseCode("OK");
        apiResp.setResult(result);

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(SunbirdApiResp.class)))
                .thenReturn(apiResp);

        boolean valid = userUtilityService.validateUser("rootOrg", "userId");
        assertTrue(valid);
    }

    @Test
    void testValidateUser_InvalidUser() {
        SunbirdApiResultResponse response = new SunbirdApiResultResponse();
        response.setCount(0);
        SunbirdApiRespResult result = new SunbirdApiRespResult();
        result.setResponse(response);
        SunbirdApiResp apiResp = new SunbirdApiResp();
        apiResp.setResponseCode("OK");
        apiResp.setResult(result);

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(SunbirdApiResp.class)))
                .thenReturn(apiResp);

        boolean valid = userUtilityService.validateUser("rootOrg", "userId");
        assertFalse(valid);
    }

    @Test
    void testValidateUser_ApiError() {
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(SunbirdApiResp.class)))
                .thenThrow(new RuntimeException("API error"));

        assertThrows(CustomException.class, () -> userUtilityService.validateUser("rootOrg", "userId"));
    }
}
