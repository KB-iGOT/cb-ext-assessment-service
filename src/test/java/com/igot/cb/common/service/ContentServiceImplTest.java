package com.igot.cb.common.service;

import com.igot.cb.cache.DataCacheMgr;
import com.igot.cb.cache.RedisCacheMgr;
import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.util.CbExtAssessmentServerProperties;
import com.igot.cb.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentServiceImplTest {

    private static final String USER_ID = "9f719c1b-92aa-483f-ae9b-dfccc5169a49";
    private static final String USER_AUTH_TOKEN = "token-123";
    private static final String ASSESSMENT_ID = "do_1143924314542161921999";
    private static final String COURSE_ID = "do_113936617403949056126";
    private static final String BATCH_ID = "0139366281149644805";
    private static final Double OVERALL_RESULT_SCORE = 85.0;
    private static final Double FAILED_ATTEMPT_SCORE = 20.0;
    private static final String COURSE_SERVICE_HOST = "http://localhost:9000/";
    private static final String PROGRESS_UPDATE_ENDPOINT = "/v1/content/state/admin/update";
    private static final String SB_API_KEY = "sb-api-key";

    @Mock
    private OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Mock
    private CbExtAssessmentServerProperties serverConfig;

    @Mock
    private RedisCacheMgr redisCacheMgr;

    @Mock
    private DataCacheMgr dataCacheMgr;

    @InjectMocks
    private ContentServiceImpl contentService;

    private Map<String, Object> reqBody;
    private SBApiResponse outgoingResponse;

    @BeforeEach
    void setUp() {
        reqBody = new HashMap<>();
        reqBody.put(Constants.IDENTIFIER, ASSESSMENT_ID);
        reqBody.put(Constants.COURSE_ID, COURSE_ID);
        reqBody.put(Constants.BATCH_ID, BATCH_ID);

        outgoingResponse = new SBApiResponse();

        when(serverConfig.getCourseServiceHost()).thenReturn(COURSE_SERVICE_HOST);
        when(serverConfig.getProgressUpdateEndPoint()).thenReturn(PROGRESS_UPDATE_ENDPOINT);
        when(serverConfig.getSbApiKey()).thenReturn(SB_API_KEY);
    }

    @Test
    void updateContentProgress_overallResultPresent_sendsAdditionalPropertiesWithAssessmentIdAndScore() {
        outgoingResponse.put(Constants.OVERALL_RESULT, OVERALL_RESULT_SCORE);
        mockOutboundSuccess();

        String result = contentService.updateContentProgress(USER_AUTH_TOKEN, reqBody, USER_ID, outgoingResponse);

        assertThat(result).isEqualTo(Constants.SUCCESS);
        Map<String, Object> additionalProperties = capturedAdditionalProperties();
        assertThat(additionalProperties).isNotNull();
        assertThat(additionalProperties.get(Constants.ASSESSMENT_ID_KEY)).isEqualTo(ASSESSMENT_ID);
        assertThat(additionalProperties.get(Constants.CURRENT_SCORE)).isEqualTo(OVERALL_RESULT_SCORE);
    }

    @Test
    void updateContentProgress_overallResultAbsent_omitsAdditionalProperties() {
        mockOutboundSuccess();

        String result = contentService.updateContentProgress(USER_AUTH_TOKEN, reqBody, USER_ID, outgoingResponse);

        assertThat(result).isEqualTo(Constants.SUCCESS);
        Map<String, Object> contentEntry = capturedFirstContentEntry();
        assertThat(contentEntry).doesNotContainKey(Constants.ADDITIONAL_PROPERTIES);
    }

    @Test
    void updateContentProgress_overallResultPresentOnFailedAttempt_stillSendsScore() {
        // KB-15848: score must be sent for a failed attempt too, not only on pass.
        outgoingResponse.put(Constants.OVERALL_RESULT, FAILED_ATTEMPT_SCORE);
        outgoingResponse.put(Constants.PASS, false);
        mockOutboundSuccess();

        contentService.updateContentProgress(USER_AUTH_TOKEN, reqBody, USER_ID, outgoingResponse);

        Map<String, Object> additionalProperties = capturedAdditionalProperties();
        assertThat(additionalProperties.get(Constants.CURRENT_SCORE)).isEqualTo(FAILED_ATTEMPT_SCORE);
    }

    @Test
    void updateContentProgress_apiResponseNotOk_returnsFailureAndClearsOutgoingResult() {
        outgoingResponse.put(Constants.OVERALL_RESULT, OVERALL_RESULT_SCORE);
        Map<String, Object> failureApiResponse = new HashMap<>();
        failureApiResponse.put(Constants.RESPONSE_CODE, "CLIENT_ERROR");
        when(outboundRequestHandlerService.fetchResultUsingPatch(anyString(), any(), anyMap()))
                .thenReturn(failureApiResponse);

        String result = contentService.updateContentProgress(USER_AUTH_TOKEN, reqBody, USER_ID, outgoingResponse);

        assertThat(result).isEmpty();
        assertThat(outgoingResponse.getResult()).isNull();
    }

    @Test
    void updateContentProgress_outboundCallThrows_returnsFailureAndClearsOutgoingResult() {
        outgoingResponse.put(Constants.OVERALL_RESULT, OVERALL_RESULT_SCORE);
        when(outboundRequestHandlerService.fetchResultUsingPatch(anyString(), any(), anyMap()))
                .thenThrow(new RuntimeException("downstream unavailable"));

        String result = contentService.updateContentProgress(USER_AUTH_TOKEN, reqBody, USER_ID, outgoingResponse);

        assertThat(result).isEmpty();
        assertThat(outgoingResponse.getResult()).isNull();
    }

    private void mockOutboundSuccess() {
        Map<String, Object> successApiResponse = new HashMap<>();
        successApiResponse.put(Constants.RESPONSE_CODE, Constants.OK);
        when(outboundRequestHandlerService.fetchResultUsingPatch(anyString(), any(), anyMap()))
                .thenReturn(successApiResponse);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> capturedFirstContentEntry() {
        ArgumentCaptor<Map<String, Object>> requestCaptor = ArgumentCaptor.forClass(Map.class);
        verify(outboundRequestHandlerService).fetchResultUsingPatch(anyString(), requestCaptor.capture(), anyMap());
        Map<String, Object> request = (Map<String, Object>) requestCaptor.getValue().get(Constants.REQUEST);
        List<Map<String, Object>> contents = (List<Map<String, Object>>) request.get("contents");
        return contents.get(0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> capturedAdditionalProperties() {
        return (Map<String, Object>) capturedFirstContentEntry().get(Constants.ADDITIONAL_PROPERTIES);
    }
}
