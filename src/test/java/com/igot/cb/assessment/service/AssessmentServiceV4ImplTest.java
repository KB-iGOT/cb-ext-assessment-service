package com.igot.cb.assessment.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.assessment.repo.AssessmentRepository;
import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.service.ContentService;
import com.igot.cb.common.service.OutboundRequestHandlerServiceImpl;
import com.igot.cb.common.util.AccessTokenValidator;
import com.igot.cb.common.util.CbExtAssessmentServerProperties;
import com.igot.cb.common.util.Constants;
import com.igot.cb.core.producer.Producer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;


import java.time.Instant;
import java.util.*;

class AssessmentServiceV4ImplTest {

    @InjectMocks
    AssessmentServiceV4Impl service;

    @Mock
    CbExtAssessmentServerProperties serverProperties;
    @Mock
    Producer kafkaProducer;
    @Mock
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;
    @Mock
    AssessmentUtilServiceV2 assessUtilServ;
    @Mock
    ObjectMapper mapper;
    @Mock
    AssessmentRepository assessmentRepository;
    @Mock
    AccessTokenValidator accessTokenValidator;
    @Mock
    ContentService contentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(serverProperties.getAssessmentLevelParams()).thenReturn(Arrays.asList("primaryCategory", "expectedDuration", "children"));
        when(serverProperties.getAssessmentSectionParams()).thenReturn(Arrays.asList("identifier", "primaryCategory", "minimumPassPercentage", "objectType", "children"));
        when(serverProperties.getUserAssessmentSubmissionDuration()).thenReturn("5");
    }

    @Test
    void testRetakeAssessment_Positive() {
        String token = "token";
        String userId = "user1";
        String assessmentId = "assess1";
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchy.put(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS, 3);

        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        when(assessUtilServ.readAssessmentHierarchyFromCache(eq(assessmentId), anyBoolean(), eq(token))).thenReturn(hierarchy);
        when(serverProperties.isAssessmentRetakeCountVerificationEnabled()).thenReturn(false);

        SBApiResponse resp = service.retakeAssessment(assessmentId, token, false);
        assertEquals(3, resp.getResult().get(Constants.TOTAL_RETAKE_ATTEMPTS_ALLOWED));
    }

    @Test
    void testRetakeAssessment_Negative_BlankUserId() {
        String token = "token";
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn("");
        SBApiResponse resp = service.retakeAssessment("assess1", token, false);
        assertEquals(Constants.USER_ID_DOESNT_EXIST, resp.getParams().getErrmsg());
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
    }

    @Test
    void testRetakeAssessment_Negative_EmptyHierarchy() {
        String token = "token";
        String userId = "user1";
        String assessmentId = "assess1";
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        when(assessUtilServ.readAssessmentHierarchyFromCache(eq(assessmentId), anyBoolean(), eq(token))).thenReturn(Collections.emptyMap());

        SBApiResponse resp = service.retakeAssessment(assessmentId, token, false);
        assertEquals(Constants.ASSESSMENT_HIERARCHY_READ_FAILED, resp.getParams().getErrmsg());
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
    }

    @Test
    void testRetakeAssessment_Positive_PreEnrolled() {
        String token = "token";
        String userId = "user1";
        String assessmentId = "assess1";
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchy.put(Constants.CONTEXT_CATEGORY_TAG, Constants.PRE_ENROLLED_ASSESSMENT_KEY);

        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        when(assessUtilServ.readAssessmentHierarchyFromCache(eq(assessmentId), anyBoolean(), eq(token))).thenReturn(hierarchy);

        SBApiResponse resp = service.retakeAssessment(assessmentId, token, false);
        assertEquals(1, resp.getResult().get(Constants.TOTAL_RETAKE_ATTEMPTS_ALLOWED));
        assertEquals(0, resp.getResult().get(Constants.RETAKE_ATTEMPTS_CONSUMED));
    }

    @Test
    void testRetakeAssessment_Exception() {
        String token = "token";
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenThrow(new RuntimeException("fail"));
        SBApiResponse resp = service.retakeAssessment("assess1", token, false);
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
        assertTrue(resp.getParams().getErrmsg().contains("Error while calculating retake assessment"));
    }

    @Test
    void testReadAssessment_Negative_MissingExpectedDuration() {
        String token = "token";
        String userId = "user1";
        String assessmentId = "assess1";
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchy.put(Constants.CHILDREN, Arrays.asList());
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        when(assessUtilServ.readAssessmentHierarchyFromCache(eq(assessmentId), anyBoolean(), eq(token))).thenReturn(hierarchy);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(userId, assessmentId)).thenReturn(Collections.emptyList());

        SBApiResponse resp = service.readAssessment(assessmentId, token, false, null);
        assertEquals(Constants.ASSESSMENT_INVALID, resp.getParams().getErrmsg());
    }

    @Test
    void testReadAssessment_Negative_EmptyHierarchy() {
        String token = "token";
        String userId = "user1";
        String assessmentId = "assess1";
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        when(assessUtilServ.readAssessmentHierarchyFromCache(eq(assessmentId), anyBoolean(), eq(token))).thenReturn(Collections.emptyMap());

        SBApiResponse resp = service.readAssessment(assessmentId, token, false, null);
        assertEquals(Constants.ASSESSMENT_HIERARCHY_READ_FAILED, resp.getParams().getErrmsg());
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
    }

    @Test
    void testReadAssessment_Positive_ExistingData() throws Exception {
        String token = "token";
        String userId = "user1";
        String assessmentId = "assess1";
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchy.put(Constants.EXPECTED_DURATION, 10);
        Map<String, Object> section = new HashMap<>();
        section.put("identifier", "section1");
        section.put("primaryCategory", "Section");
        section.put("children", Arrays.asList(Collections.singletonMap("identifier", "q1")));
        section.put("minimumPassPercentage", 50);
        section.put("objectType", "Section");
        hierarchy.put(Constants.CHILDREN, Arrays.asList(section));

        Map<String, Object> existingData = new HashMap<>();
        existingData.put(Constants.END_TIME, Date.from(Instant.now().plusSeconds(60)));
        existingData.put(Constants.STATUS, Constants.NOT_SUBMITTED);
        existingData.put(Constants.ASSESSMENT_READ_RESPONSE_KEY, "{}");

        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        when(assessUtilServ.readAssessmentHierarchyFromCache(eq(assessmentId), anyBoolean(), eq(token))).thenReturn(hierarchy);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(userId, assessmentId)).thenReturn(Arrays.asList(existingData));
        when(assessmentRepository.addUserAssesmentDataToDB(any(), any(), any(), any(), any(), any())).thenReturn(true);

        SBApiResponse resp = service.readAssessment(assessmentId, token, false, null);
        assertNotNull(resp.getResult().get(Constants.QUESTION_SET));
    }

    @Test
    void testReadAssessment_Exception() {
        String token = "token";
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenThrow(new RuntimeException("fail"));
        SBApiResponse resp = service.readAssessment("assess1", token, false, null);
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
        assertTrue(resp.getParams().getErrmsg().contains("Error while reading assessment"));
    }

    @Test
    void testReadWheebox_Positive() {
        String token = "token";
        String userId = "user1";
        Map<String, Object> wheeboxData = new HashMap<>();
        wheeboxData.put("score", 90);

        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        when(assessUtilServ.fetchWheebox(userId)).thenReturn(wheeboxData);

        SBApiResponse resp = service.readWheebox(token);
        assertTrue(resp.getResult().containsKey(Constants.RESPONSE));
    }

    @Test
    void testReadWheebox_Negative_BlankUserId() {
        String token = "token";
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn("");
        SBApiResponse resp = service.readWheebox(token);
        assertEquals(Constants.USER_ID_DOESNT_EXIST, resp.getParams().getErrmsg());
    }

    @Test
    void testReadWheebox_Exception() {
        String token = "token";
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenThrow(new RuntimeException("fail"));
        SBApiResponse resp = service.readWheebox(token);
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsync_valid() {
        assertDoesNotThrow(() -> service.submitAssessmentAsync(new HashMap<>(), "user", true));
    }


    @Test
    void testHandleAssessmentSubmitRequest_valid() {
        assertDoesNotThrow(() -> service.handleAssessmentSubmitRequest(new HashMap<>(), true, "user"));
    }

}