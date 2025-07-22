package com.igot.cb.assessment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;


import com.igot.cb.assessment.repo.AssessmentRepository;
import com.igot.cb.cassandra.utils.CassandraOperation;
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

class AssessmentServiceV5ImplTest {

    @InjectMocks
    private AssessmentServiceV5Impl service;

    @Mock
    private AccessTokenValidator accessTokenValidator;
    @Mock
    private AssessmentUtilServiceV2 assessUtilServ;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private CbExtAssessmentServerProperties serverProperties;
    @Mock
    private CassandraOperation cassandraOperation;
    @Mock
    private OutboundRequestHandlerServiceImpl outboundRequestHandlerService;
    @Mock
    private Producer producer;
    @Mock
    private ContentService contentService;

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

    @Test
    void testReadQuestionList_InvalidUser() {
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

    @Test
    void testReadAssessment_UserIdBlank() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");
        SBApiResponse response = service.readAssessment("id", "token", false, "ctx");
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
    }

    @Test
    void testReadQuestionList_ErrorMessage() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");
        Map<String, Object> requestBody = new HashMap<>();
        SBApiResponse response = service.readQuestionList(requestBody, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    @Test
    void testSaveAssessmentAsync_UserIdBlank() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");
        SBApiResponse response = service.saveAssessmentAsync(new HashMap<>(), "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    @Test
    void testAutoPublish_AssessmentIdentifierBlank() {
        SBApiResponse response = service.autoPublish("", "token");
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
    }

    @Test
    void testSubmitAssessmentAsync_UserIdEmpty() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn(null);
        SBApiResponse response = service.submitAssessmentAsync(new HashMap<>(), "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsync_SuccessPracticeAssessment() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        submitRequest.put(Constants.LANGUAGE, "english");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, Constants.PRACTICE_QUESTION_SET);
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        assessmentHierarchy.put(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS, 3);
        assessmentHierarchy.put(Constants.ASSESSMENT_TYPE, "defaultType");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        when(assessUtilServ.readAssessmentRecord("assessmentId", List.of(Constants.LANGUAGE)))
                .thenReturn("english");
        SBApiResponse response = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsync_InvalidSectionData() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, "OtherCategory");
        assessmentHierarchy.put(Constants.CHILDREN, null); // Simulate missing children
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        SBApiResponse response = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsync_ExceptionHandling() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenThrow(new RuntimeException("Simulated error"));
        SBApiResponse response = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains("Failed to process assessment submit request"));
    }

    @Test
    void testSubmitAssessmentAsync_NonPracticeAssessment_UserDataMissing(){
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, "NonPractice");
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        assessmentHierarchy.put(Constants.EXPECTED_DURATION, 1);
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        // Simulate expired submission by mocking validateSubmitAssessmentRequest to return expired
        SBApiResponse response = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.USER_ASSESSMENT_DATA_NOT_PRESENT, response.getParams().getErrmsg());
    }

    @Test
    void testSubmitAssessmentAsync_NonPracticeAssessment_AssessmentExpired() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());

        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, "NonPractice");
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        assessmentHierarchy.put(Constants.EXPECTED_DURATION, 1); // 1 second duration

        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);

        // Mock user assessment record with old start time (expired)
        Map<String, Object> userAssessment = new HashMap<>();
        userAssessment.put(Constants.START_TIME, java.time.Instant.now().minusSeconds(120).toString()); // 2 minutes ago
        List<Map<String, Object>> userAssessmentList = List.of(userAssessment);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(userAssessmentList);

        SBApiResponse response = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.ASSESSMENT_SUBMIT_EXPIRED, response.getParams().getErrmsg());
    }

    @Test
    void testValidateSubmitAssessmentRequest_Reflection() throws Exception {
        // Prepare arguments as per the method signature
        Map<String, Object> submitRequest = new HashMap<>();
        String userId = "user";
        List<Map<String, Object>> sectionList = new ArrayList<>();
        List<Map<String, Object>> questionList = new ArrayList<>();
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        Map<String, Object> userAssessment = new HashMap<>();
        String assessmentId = "assessmentId";
        boolean isRetake = false;

        // Get the private method
        Method method = AssessmentServiceV5Impl.class.getDeclaredMethod(
                "validateSubmitAssessmentRequest",
                Map.class, String.class, List.class, List.class, Map.class, Map.class, String.class, boolean.class
        );
        method.setAccessible(true);

        // Invoke the method
        Object result = method.invoke(service, submitRequest, userId, sectionList, questionList, assessmentHierarchy, userAssessment, assessmentId, isRetake);

        // Assert result as needed
        assertNotNull(result);
    }

    @Test
    void testSubmitAssessmentAsync_FailedNonPracticeAssessment() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        submitRequest.put(Constants.LANGUAGE, "english");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, "Competency Assessment");
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        assessmentHierarchy.put(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS, 2);
        assessmentHierarchy.put(Constants.ASSESSMENT_TYPE, "defaultType");
        assessmentHierarchy.put(Constants.EXPECTED_DURATION, 60);
        List<Map<String, Object>> userAssessmentList = new ArrayList<>();
        Map<String, Object> userAssessment = new HashMap<>();
        userAssessment.put(Constants.START_TIME, java.time.Instant.now().toString()); // or new Date(), or Instant
        userAssessmentList.add(userAssessment);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(userAssessmentList);
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        when(assessUtilServ.readAssessmentRecord("assessmentId", List.of(Constants.LANGUAGE)))
                .thenReturn("english");
        SBApiResponse response = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsync_SuccessWithDifferentAssessmentType() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        submitRequest.put(Constants.LANGUAGE, "english");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, Constants.PRACTICE_QUESTION_SET);
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        assessmentHierarchy.put(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS, 5);
        assessmentHierarchy.put(Constants.ASSESSMENT_TYPE, "questionWeightage");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        when(assessUtilServ.readAssessmentRecord("assessmentId", List.of(Constants.LANGUAGE)))
                .thenReturn("english");
        SBApiResponse response = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsyncV6_InvalidUser() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn(null);
        SBApiResponse response = service.submitAssessmentAsyncV6(new HashMap<>(), "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.USER_ID_DOESNT_EXIST, response.getParams().getErrmsg());
    }

    @Test
    void testSubmitAssessmentAsyncV6_MissingAssessmentId() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        SBApiResponse response = service.submitAssessmentAsyncV6(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.INVALID_ASSESSMENT_ID, response.getParams().getErrmsg());
    }

    @Test
    void testSubmitAssessmentAsyncV6_AssessmentHierarchyMissing() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(Collections.emptyMap());
        SBApiResponse response = service.submitAssessmentAsyncV6(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsyncV6_LanguageMismatch() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        submitRequest.put(Constants.LANGUAGE, "english");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, Constants.PRACTICE_QUESTION_SET);
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        assessmentHierarchy.put(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS, 3);
        assessmentHierarchy.put(Constants.ASSESSMENT_TYPE, "defaultType");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        when(assessUtilServ.readAssessmentRecord("assessmentId", List.of(Constants.LANGUAGE)))
                .thenReturn("hindi");
        SBApiResponse response = service.submitAssessmentAsyncV6(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains("Assessment language mismatch"));
    }

    @Test
    void testSubmitAssessmentAsyncV6_SuccessPracticeAssessment() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        submitRequest.put(Constants.LANGUAGE, "english");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        submitRequest.put(Constants.COURSE_ID, "courseId");
        when(contentService.readContent(any())).thenReturn(Collections.emptyMap());
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, Constants.PRACTICE_QUESTION_SET);
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        assessmentHierarchy.put(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS, 3);
        assessmentHierarchy.put(Constants.ASSESSMENT_TYPE, "defaultType");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        when(assessUtilServ.readAssessmentRecord("assessmentId", List.of(Constants.LANGUAGE)))
                .thenReturn("english");
        SBApiResponse response = service.submitAssessmentAsyncV6(submitRequest, "token", false);
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsyncV6_ExceptionHandling() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assessmentId");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenThrow(new RuntimeException("Simulated error"));
        SBApiResponse response = service.submitAssessmentAsyncV6(submitRequest, "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains("Failed to process assessment submit request"));
    }

    @Test
    void testReadAssessment_HierarchyMissing() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(Collections.emptyMap());
        SBApiResponse response = service.readAssessment("assess1", "token", false, null);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.ASSESSMENT_HIERARCHY_READ_FAILED, response.getParams().getErrmsg());
    }

    @Test
    void testReadAssessment_PracticeAssessment() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, Constants.PRACTICE_QUESTION_SET);
        hierarchy.put(Constants.CHILDREN,  new ArrayList<>());
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);
        SBApiResponse response = service.readAssessment("assess1", "token", false, null);
        assertNotNull(response.getResult().get(Constants.QUESTION_SET));
    }

    @Test
    void testReadAssessment_FirstTimeRead_MissingDuration() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        SBApiResponse response = service.readAssessment("assess1", "token", false, null);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.ASSESSMENT_INVALID, response.getParams().getErrmsg());
    }

    @Test
    void testReadAssessment_FirstTimeRead_ContextLockError() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchy.put(Constants.EXPECTED_DURATION, 60);
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(assessUtilServ.validateContextLocking(anyMap(), any(), any(), anyString()))
                .thenReturn("LOCKED");
        SBApiResponse response = service.readAssessment("assess1", "token", false, null);
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
    }

    @Test
    void testReadAssessment_FirstTimeRead_DBUpdateFails() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchy.put(Constants.EXPECTED_DURATION, 60);
        hierarchy.put(Constants.CHILDREN, new ArrayList<>());
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(assessUtilServ.validateContextLocking(anyMap(), any(), any(), anyString()))
                .thenReturn("");
        when(assessmentRepository.addUserAssesmentDataToDB(anyString(), anyString(), any(), any(), anyMap(), anyString()))
                .thenReturn(false);
        SBApiResponse response = service.readAssessment("assess1", "token", false, null);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.ASSESSMENT_DATA_START_TIME_NOT_UPDATED, response.getParams().getErrmsg());
    }

    @Test
    void testReadAssessment_ExistingData_NotSubmitted() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchy.put(Constants.EXPECTED_DURATION, 60);
        hierarchy.put(Constants.CHILDREN, new ArrayList<>());
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);
        Map<String, Object> existing = new HashMap<>();
        existing.put(Constants.END_TIME, Date.from(Instant.now().plusSeconds(60)));
        existing.put(Constants.STATUS, Constants.NOT_SUBMITTED);
        existing.put(Constants.ASSESSMENT_READ_RESPONSE_KEY, "{\"foo\":\"bar\"}");
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(List.of(existing));
        SBApiResponse response = service.readAssessment("assess1", "token", false, null);
        assertNotNull(response.getResult().get(Constants.QUESTION_SET));
    }

    @Test
    void testReadAssessment_ExistingData_StartTimeNotUpdated() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchy.put(Constants.EXPECTED_DURATION, 60);
        hierarchy.put(Constants.CHILDREN, new ArrayList<>());
        hierarchy.put(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS, 1);
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);
        Map<String, Object> existing = new HashMap<>();
        existing.put(Constants.END_TIME, Date.from(Instant.now().minusSeconds(60)));
        existing.put(Constants.STATUS, Constants.SUBMITTED);
        existing.put(Constants.ASSESSMENT_READ_RESPONSE_KEY, "{\"foo\":\"bar\"}");
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(List.of(existing));
        SBApiResponse response = service.readAssessment("assess1", "token", false, null);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.ASSESSMENT_DATA_START_TIME_NOT_UPDATED, response.getParams().getErrmsg());
    }

    @Test
    void testReadAssessment_ExistingData_RetakeNormal() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchy.put(Constants.EXPECTED_DURATION, 60);
        hierarchy.put(Constants.CHILDREN, new ArrayList<>());
        hierarchy.put(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS, 2);
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);
        Map<String, Object> existing = new HashMap<>();
        existing.put(Constants.END_TIME, Date.from(Instant.now().minusSeconds(60)));
        existing.put(Constants.STATUS, Constants.SUBMITTED);
        existing.put(Constants.ASSESSMENT_READ_RESPONSE_KEY, "{\"foo\":\"bar\"}");
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(List.of(existing));
        when(assessUtilServ.validateContextLocking(anyMap(), any(), any(), anyString()))
                .thenReturn("");
        when(assessmentRepository.addUserAssesmentDataToDB(anyString(), anyString(), any(), any(), anyMap(), anyString()))
                .thenReturn(true);
        SBApiResponse response = service.readAssessment("assess1", "token", false, null);
        assertNotNull(response.getResult().get(Constants.QUESTION_SET));
    }

    @Test
    void testSaveAssessmentAsync_Positive() {
        // Arrange
        String userId = "user1";
        String assessmentId = "assessment123";
        String token = "token";
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, assessmentId);

        // Mock user ID
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);

        // Mock assessment details
        Map<String, Object> assessmentAllDetail = new HashMap<>();
        assessmentAllDetail.put(Constants.PRIMARY_CATEGORY, "Assessment");
        when(assessUtilServ.readAssessmentHierarchyFromCache(assessmentId, false, token))
                .thenReturn(assessmentAllDetail);

        // Mock user assessment record
        Date startTime = new Date(System.currentTimeMillis() - 10000);
        Date endTime = new Date(System.currentTimeMillis() + 10000);
        Map<String, Object> userAssessment = new HashMap<>();
        userAssessment.put(Constants.START_TIME, startTime);
        userAssessment.put(Constants.END_TIME, endTime);
        userAssessment.put(Constants.STATUS, Constants.NOT_SUBMITTED);
        userAssessment.put(Constants.ASSESSMENT_READ_RESPONSE_KEY, "{\"foo\":\"bar\"}");
        List<Map<String, Object>> userAssessmentList = List.of(userAssessment);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(userId, assessmentId)).thenReturn(userAssessmentList);

        // Mock DB update
        when(assessmentRepository.updateUserAssesmentDataToDB(
                eq(userId), eq(assessmentId), any(), any(), any(), any(), eq(submitRequest)))
                .thenReturn(true);

        // Act
        SBApiResponse response = service.saveAssessmentAsync(submitRequest, token, false);

        // Assert
        assertNotNull(response);
        assertTrue(response.getResult().containsKey(Constants.QUESTION_SET));
        assertEquals(true, response.getResult().get("ASSESSMENT_UPDATE"));
    }

    @Test
    void testSaveAssessmentAsync_AssessmentHierarchyMissing() {
        // Arrange
        String userId = "user1";
        String assessmentId = "assessment123";
        String token = "token";
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, assessmentId);

        // Mock user ID
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);

        // Mock assessment hierarchy as empty
        when(assessUtilServ.readAssessmentHierarchyFromCache(assessmentId, false, token))
                .thenReturn(Collections.emptyMap());

        // Act
        SBApiResponse response = service.saveAssessmentAsync(submitRequest, token, false);

        // Assert
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.ASSESSMENT_HIERARCHY_READ_FAILED, response.getParams().getErrmsg());
    }

    @Test
    void testGetParamDetailsForQTypes_QuestionWeightage() throws Exception {
        // Prepare mocks and data
        Map<String, Object> hierarchySection = new HashMap<>();
        hierarchySection.put(Constants.TOTAL_MARKS, 100);

        // Mock section-level definition
        Map<String, Object> proficiencyMap = new HashMap<>();
        proficiencyMap.put("marksForQuestion", 5);
        Map<String, Map<String, Object>> sectionLevelDefinition = new HashMap<>();
        sectionLevelDefinition.put("proficiency1", proficiencyMap);
        hierarchySection.put(Constants.SECTION_LEVEL_DEFINITION, sectionLevelDefinition);

        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.ASSESSMENT_TYPE, Constants.QUESTION_WEIGHTAGE);
        assessmentHierarchy.put(Constants.MINIMUM_PASS_PERCENTAGE, 60);
        assessmentHierarchy.put(Constants.NEGATIVE_MARKING_PERCENTAGE, 10);

        String hierarchySectionId = "section1";

        // Use reflection to access the private method
        Method method = AssessmentServiceV5Impl.class.getDeclaredMethod(
                "getParamDetailsForQTypes",
                Map.class, Map.class, String.class
        );
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(
                service, hierarchySection, assessmentHierarchy, hierarchySectionId
        );

        // Assertions
        assertEquals(Constants.QUESTION_WEIGHTAGE, result.get(Constants.ASSESSMENT_TYPE));
        assertEquals(60, result.get(Constants.MINIMUM_PASS_PERCENTAGE));
        assertEquals(100, result.get(Constants.TOTAL_MARKS));
        assertEquals(10, result.get(Constants.NEGATIVE_MARKING_PERCENTAGE));
        assertEquals(hierarchySectionId, result.get("hierarchySectionId"));
        assertTrue(result.containsKey(Constants.QUESTION_SECTION_SCHEME));
    }

    @Test
    void testAutoPublish_Success() {
        String assessmentId = "assess123";
        String token = "token";
        String userId = "user1";
        String rootOrgId = "org123";

        // Mock user ID
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);

        // Mock publish response
        Map<String, Object> publishResult = new HashMap<>();
        publishResult.put(Constants.RESULT, Map.of("published", true));
        publishResult.put(Constants.RESPONSE_CODE, Constants.OK);
        when(service.publish(assessmentId, token)).thenReturn(publishResult);

        // Mock Cassandra response
        Map<String, Object> orgMap = new HashMap<>();
        orgMap.put(Constants.ROOT_ORG_ID, rootOrgId);
        when(cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                anyString(), anyString(), anyMap(), anyList()))
                .thenReturn(List.of(orgMap));

        // Mock update org response
        Map<String, Object> updateOrgResponse = new HashMap<>();
        updateOrgResponse.put(Constants.RESPONSE_CODE, Constants.OK);
        when(outboundRequestHandlerService.fetchResultUsingPatch(
                anyString(), anyMap(), anyMap()))
                .thenReturn(updateOrgResponse);

        // Mock server properties
        when(serverProperties.getSbUrl()).thenReturn("http://example.com/");
        when(serverProperties.getUpdateOrgPath()).thenReturn("updateOrg");
        when(serverProperties.getCqfAssessmentPostPublishTopic()).thenReturn("topic");

        // Act
        SBApiResponse response = service.autoPublish(assessmentId, token);

        // Assert
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
        assertNotNull(response.getResult());
        verify(producer).push("topic", assessmentId);
    }

    @Test
    void testUserIdBlank() throws Exception {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");
        Map<String, Object> request = new HashMap<>();
        List<String> identifierList = new ArrayList<>();
        AssessmentServiceV5Impl spyService = Mockito.spy(service);
        Method method = AssessmentServiceV5Impl.class.getDeclaredMethod(
                "validateQuestionListAPI",
                Map.class, String.class, List.class, boolean.class
        );
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) method.invoke(
                spyService, request, "token", identifierList, false
        );
        assertEquals(Constants.USER_ID_DOESNT_EXIST, result.get(Constants.ERROR_MESSAGE));
    }

    @Test
    void testAssessmentIdBlank() throws Exception {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> request = new HashMap<>();
        List<String> identifierList = new ArrayList<>();
        AssessmentServiceV5Impl spyService = Mockito.spy(service);
        Method method = AssessmentServiceV5Impl.class.getDeclaredMethod(
                "validateQuestionListAPI",
                Map.class, String.class, List.class, boolean.class
        );
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) method.invoke(
                spyService, request, "token", identifierList, false
        );
        assertEquals(Constants.ASSESSMENT_ID_KEY_IS_NOT_PRESENT_IS_EMPTY, result.get(Constants.ERROR_MESSAGE));
    }

    @Test
    void testIdentifierListEmpty() throws Exception {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user");
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.ASSESSMENT_ID_KEY, "assess1");
        // getQuestionIdList returns empty
        AssessmentServiceV5Impl spyService = Mockito.spy(service);
        List<String> identifierList = new ArrayList<>();
        Method method = AssessmentServiceV5Impl.class.getDeclaredMethod(
                "validateQuestionListAPI",
                Map.class, String.class, List.class, boolean.class
        );
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) method.invoke(
                spyService, request, "token", identifierList, false
        );
        assertEquals(Constants.IDENTIFIER_LIST_IS_EMPTY, result.get(Constants.ERROR_MESSAGE));
    }

    @Test
    void testCreateResponseMapWithProperStructure_WithResultMap() {
        Map<String, Object> hierarchySection = new HashMap<>();
        hierarchySection.put(Constants.IDENTIFIER, "section1");
        hierarchySection.put(Constants.OBJECT_TYPE, "Section");
        hierarchySection.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchySection.put(Constants.MINIMUM_PASS_PERCENTAGE, 60);
        hierarchySection.put(Constants.NAME, "Section Name");

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(Constants.RESULT, 75.0);
        resultMap.put(Constants.BLANK, 2);
        resultMap.put(Constants.CORRECT, 5);
        resultMap.put(Constants.INCORRECT, 3);
        resultMap.put(Constants.CHILDREN, List.of("q1", "q2"));
        resultMap.put(Constants.SECTION_RESULT, "PASS");
        resultMap.put(Constants.TOTAL_MARKS, 100);
        resultMap.put(Constants.SECTION_MARKS, 75);

        Map<String, Object> result = service.createResponseMapWithProperStructure(hierarchySection, resultMap);

        assertEquals("section1", result.get(Constants.IDENTIFIER));
        assertEquals("Section", result.get(Constants.OBJECT_TYPE));
        assertEquals("Assessment", result.get(Constants.PRIMARY_CATEGORY));
        assertEquals(60, result.get(Constants.PASS_PERCENTAGE));
        assertEquals("Section Name", result.get(Constants.NAME));
        assertEquals(75.0, result.get(Constants.RESULT));
        assertEquals(2, result.get(Constants.BLANK));
        assertEquals(5, result.get(Constants.CORRECT));
        assertEquals(3, result.get(Constants.INCORRECT));
        assertEquals(List.of("q1", "q2"), result.get(Constants.CHILDREN));
        assertEquals("PASS", result.get(Constants.SECTION_RESULT));
        assertEquals(100, result.get(Constants.TOTAL_MARKS));
        assertEquals(75, result.get(Constants.SECTION_MARKS));
        assertEquals(true, result.get(Constants.PASS));
        assertEquals(75.0, result.get(Constants.OVERALL_RESULT));
    }

    @Test
    void testCreateResponseMapWithProperStructure_EmptyResultMap() {
        Map<String, Object> hierarchySection = new HashMap<>();
        hierarchySection.put(Constants.IDENTIFIER, "section2");
        hierarchySection.put(Constants.OBJECT_TYPE, "Section");
        hierarchySection.put(Constants.PRIMARY_CATEGORY, "Assessment");
        hierarchySection.put(Constants.MINIMUM_PASS_PERCENTAGE, 50);
        hierarchySection.put(Constants.NAME, "Section 2");
        hierarchySection.put(Constants.CHILDREN, List.of("q1", "q2", "q3"));

        Map<String, Object> result = service.createResponseMapWithProperStructure(hierarchySection, null);

        assertEquals("section2", result.get(Constants.IDENTIFIER));
        assertEquals("Section", result.get(Constants.OBJECT_TYPE));
        assertEquals("Assessment", result.get(Constants.PRIMARY_CATEGORY));
        assertEquals(50, result.get(Constants.PASS_PERCENTAGE));
        assertEquals("Section 2", result.get(Constants.NAME));
        assertEquals(0.0, result.get(Constants.RESULT));
        assertEquals(3, result.get(Constants.TOTAL));
        assertEquals(3, result.get(Constants.BLANK));
        assertEquals(0, result.get(Constants.CORRECT));
        assertEquals(0, result.get(Constants.INCORRECT));
        assertEquals(false, result.get(Constants.PASS));
        assertEquals(0.0, result.get(Constants.OVERALL_RESULT));
    }

    @Test
    void testRetakeAssessment_BlankUser() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");
        SBApiResponse response = service.retakeAssessment("assess1", "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.USER_ID_DOESNT_EXIST, response.getParams().getErrmsg());
    }

    @Test
    void testRetakeAssessment_AssessmentDetailMissing() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(Collections.emptyMap());
        SBApiResponse response = service.retakeAssessment("assess1", "token", false);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(Constants.ASSESSMENT_HIERARCHY_READ_FAILED, response.getParams().getErrmsg());
    }

    @Test
    void testRetakeAssessment_PreEnrolledContext() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> assessmentDetail = new HashMap<>();
        assessmentDetail.put(Constants.CONTEXT_CATEGORY_TAG, Constants.PRE_ENROLLED_ASSESSMENT_KEY);
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentDetail);
        SBApiResponse response = service.retakeAssessment("assess1", "token", false);
        assertEquals(1, response.getResult().get(Constants.TOTAL_RETAKE_ATTEMPTS_ALLOWED));
        assertEquals(0, response.getResult().get(Constants.RETAKE_ATTEMPTS_CONSUMED));
    }

    @Test
    void testRetakeAssessment_NormalRetake() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> assessmentDetail = new HashMap<>();
        assessmentDetail.put(Constants.MAX_ASSESSMENT_RETAKE_ATTEMPTS, 3);
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentDetail);

        AssessmentServiceV5Impl spyService = Mockito.spy(service);

        SBApiResponse response = spyService.retakeAssessment("assess1", "token", false);
        assertEquals(3, response.getResult().get(Constants.TOTAL_RETAKE_ATTEMPTS_ALLOWED));
        assertEquals(-1, response.getResult().get(Constants.RETAKE_ATTEMPTS_CONSUMED)); // 2 - 1
    }

    @Test
    void testCalculateAssessmentFinalResults_Pass() throws Exception {
        Map<String, Object> assessmentLevelResult = new HashMap<>();
        assessmentLevelResult.put(Constants.RESULT, 85.0);
        assessmentLevelResult.put(Constants.TOTAL, 10);
        assessmentLevelResult.put(Constants.BLANK, 1);
        assessmentLevelResult.put(Constants.CORRECT, 8);
        assessmentLevelResult.put(Constants.PASS_PERCENTAGE, 60);
        assessmentLevelResult.put(Constants.INCORRECT, 1);
        assessmentLevelResult.put(Constants.NAME, "Final Assessment");

        Method method = service.getClass().getDeclaredMethod("calculateAssessmentFinalResults", Map.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(service, assessmentLevelResult);

        assertEquals(85.0, result.get(Constants.OVERALL_RESULT));
        assertEquals(10, result.get(Constants.TOTAL));
        assertEquals(1, result.get(Constants.BLANK));
        assertEquals(8, result.get(Constants.CORRECT));
        assertEquals(60, result.get(Constants.PASS_PERCENTAGE));
        assertEquals(1, result.get(Constants.INCORRECT));
        assertEquals("Final Assessment", result.get(Constants.NAME));
        assertEquals(true, result.get(Constants.PASS));
        assertNotNull(result.get(Constants.CHILDREN));
    }

    @Test
    void testCalculateAssessmentFinalResults_NullInput() throws Exception {
        Method method = service.getClass().getDeclaredMethod("calculateAssessmentFinalResults", Map.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(service, (Map) null);

        assertNotNull(result);
        // Should be empty or handle gracefully
    }


}
