package com.igot.cb.assessment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.*;


import com.igot.cb.assessment.repo.AssessmentRepository;
import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.service.ContentService;
import com.igot.cb.common.util.AccessTokenValidator;
import com.igot.cb.common.util.CbExtAssessmentServerProperties;
import com.igot.cb.common.util.Constants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

class AssessmentServiceV5ImplTest {

    @InjectMocks
    private AssessmentServiceV5Impl service;

    @Mock
    private AccessTokenValidator accessTokenValidator;
    @Mock
    private AssessmentUtilService assessUtilServ;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private ContentService contentService;
    @Mock
    private KafkaProducer kafkaProducer;
    @Mock
    private CbExtAssessmentServerProperties serverProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(serverProperties.getUserAssessmentSubmissionDuration()).thenReturn("60");
        when(serverProperties.getAssessmentLevelParams()).thenReturn(List.of("param1", "param2"));
        when(serverProperties.getAssessmentSectionParams()).thenReturn(List.of("paramA", "paramB"));
    }

    @Test
    void testReadAssessment_NullUserId() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");
        SBApiResponse response = service.readAssessment("id", "token", false, null);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.USER_ID_DOESNT_EXIST, response.getParams().getErrmsg());
    }

//    @Test
//    void testReadAssessment_EmptyAssessmentDetails() {
//        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
//        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString())).thenReturn(Collections.emptyMap());
//        SBApiResponse response = service.readAssessment("id", "token", false, null);
//        assertEquals(Constants.FAILED, response.getParams().getStatus());
//        assertEquals(Constants.ASSESSMENT_HIERARCHY_READ_FAILED, response.getParams().getErrmsg());
//    }

//    @Test
//    void testReadAssessment_PracticeAssessment() {
//        Map<String, Object> assessmentDetail = new HashMap<>();
//        assessmentDetail.put(Constants.PRIMARY_CATEGORY, Constants.PRACTICE_QUESTION_SET);
//        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
//        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString())).thenReturn(assessmentDetail);
//        SBApiResponse response = service.readAssessment("id", "token", false, null);
//        assertTrue(response.getResult().containsKey(Constants.QUESTION_SET));
//    }

    @Test
    void testReadQuestionList_InvalidUser() throws Exception {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");
        Map<String, Object> requestBody = new HashMap<>();
        SBApiResponse response = service.readQuestionList(requestBody, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.USER_ID_DOESNT_EXIST, response.getParams().getErrmsg());
    }

    @Test
    void testReadAssessmentResultV5_UserNotFound() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");
        Map<String, Object> request = new HashMap<>();
        SBApiResponse response = service.readAssessmentResultV5(request, "token");
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.USER_ID_DOESNT_EXIST, response.getParams().getErrmsg());
    }

    @Test
    void testSubmitAssessmentAsync_InvalidUser() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn(null);
        Map<String, Object> submitRequest = new HashMap<>();
        SBApiResponse response = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.USER_ID_DOESNT_EXIST, response.getParams().getErrmsg());
    }

    @Test
    void testSaveAssessmentAsync_UserNotFound() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");
        Map<String, Object> submitRequest = new HashMap<>();
        SBApiResponse response = service.saveAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.USER_ID_DOESNT_EXIST, response.getParams().getErrmsg());
    }

    @Test
    void testReadAssessmentSavePoint_UserNotFound() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");
        SBApiResponse response = service.readAssessmentSavePoint("id", "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.USER_ID_DOESNT_EXIST, response.getParams().getErrmsg());
    }

    @Test
    void testAutoPublish_BlankAssessmentId() {
        SBApiResponse response = service.autoPublish("", "token");
        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
        assertEquals(Constants.INVALID_ASSESSMENT_ID, response.getParams().getErrmsg());
    }

    // Add more tests for edge cases and business logic as needed
}
