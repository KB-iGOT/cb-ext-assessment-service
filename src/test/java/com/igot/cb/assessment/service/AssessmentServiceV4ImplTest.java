package com.igot.cb.assessment.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.test.util.ReflectionTestUtils;


import java.lang.reflect.Method;
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
    void testReadAssessment_Positive_ExistingData() {
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

    @Test
    void testSubmitAssessmentAsync_Success_PracticeAssessment() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        submitRequest.put(Constants.LANGUAGE, "english");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, Constants.PRACTICE_QUESTION_SET);
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        assessmentHierarchy.put(Constants.SCORE_CUTOFF_TYPE, Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF);
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(assessUtilServ.readAssessmentRecord(anyString(), anyList())).thenReturn("english");
        when(contentService.readContent(anyString())).thenReturn(Collections.emptyMap());
        when(assessUtilServ.validateQumlAssessment(anyList(), anyList(), anyMap())).thenReturn(new HashMap<>());
        SBApiResponse resp = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.SUCCESS, resp.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsync_Failed_InvalidUser() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn(null);
        SBApiResponse resp = service.submitAssessmentAsync(new HashMap<>(), "token", false);
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
        assertEquals(Constants.USER_ID_DOESNT_EXIST, resp.getParams().getErrmsg());
    }

    @Test
    void testSubmitAssessmentAsync_Failed_LanguageMismatch() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        submitRequest.put(Constants.LANGUAGE, "english");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, Constants.PRACTICE_QUESTION_SET);
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        assessmentHierarchy.put(Constants.SCORE_CUTOFF_TYPE, Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF);
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        when(assessUtilServ.readAssessmentRecord(anyString(), anyList())).thenReturn("hindi");
        SBApiResponse resp = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
        assertTrue(resp.getParams().getErrmsg().contains("Assessment language mismatch"));
    }

    @Test
    void testSubmitAssessmentAsync_Failed_MissingAssessmentId() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> submitRequest = new HashMap<>();
        SBApiResponse resp = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
        assertEquals(Constants.INVALID_ASSESSMENT_ID, resp.getParams().getErrmsg());
    }

    @Test
    void testSubmitAssessmentAsync_FailedAssessmentSubmit() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        submitRequest.put(Constants.LANGUAGE, "english");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenThrow(new RuntimeException("fail"));
        SBApiResponse resp = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
        assertTrue(resp.getParams().getErrmsg().contains("Failed to process assessment submit request"));
    }

    @Test
    void testHandleAssessmentSubmitRequest_HierarchyEmpty() {
        Map<String, Object> asyncRequest = new HashMap<>();
        asyncRequest.put(Constants.USER_ID_CONSTANT, "user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        asyncRequest.put(Constants.REQUEST, submitRequest);

        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(Collections.emptyMap());

        assertDoesNotThrow(() -> service.handleAssessmentSubmitRequest(asyncRequest, false, "token"));
    }

    @Test
    void testHandleAssessmentSubmitRequest_UserAssessmentDataNotPresent() {
        Map<String, Object> asyncRequest = new HashMap<>();
        asyncRequest.put(Constants.USER_ID_CONSTANT, "user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        asyncRequest.put(Constants.REQUEST, submitRequest);

        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.CHILDREN, new ArrayList<>());
        hierarchy.put(Constants.SCORE_CUTOFF_TYPE, Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF);
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> service.handleAssessmentSubmitRequest(asyncRequest, false, "token"));
    }

    @Test
    void testHandleAssessmentSubmitRequest_AlreadySubmitted() {
        Map<String, Object> asyncRequest = new HashMap<>();
        asyncRequest.put(Constants.USER_ID_CONSTANT, "user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        asyncRequest.put(Constants.REQUEST, submitRequest);

        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.CHILDREN, new ArrayList<>());
        hierarchy.put(Constants.SCORE_CUTOFF_TYPE, Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF);
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);

        Map<String, Object> existingData = new HashMap<>();
        existingData.put(Constants.STATUS, Constants.SUBMITTED);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(List.of(existingData));

        assertDoesNotThrow(() -> service.handleAssessmentSubmitRequest(asyncRequest, false, "token"));
    }

    @Test
    void testHandleAssessmentSubmitRequest_QuestionSetNull(){
        Map<String, Object> asyncRequest = new HashMap<>();
        asyncRequest.put(Constants.USER_ID_CONSTANT, "user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        List<Map<String, Object>> children = new ArrayList<>();
        Map<String, Object> section = new HashMap<>();
        section.put(Constants.IDENTIFIER, "section1");
        section.put(Constants.CHILDREN, new ArrayList<>());
        children.add(section);
        submitRequest.put(Constants.CHILDREN, children);
        asyncRequest.put(Constants.REQUEST, submitRequest);

        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.CHILDREN, children);
        hierarchy.put(Constants.SCORE_CUTOFF_TYPE, Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF);
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);

        Map<String, Object> existingData = new HashMap<>();
        existingData.put(Constants.STATUS, "IN_PROGRESS");
        existingData.put(Constants.ASSESSMENT_READ_RESPONSE_KEY, "");
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(List.of(existingData));

        assertDoesNotThrow(() -> service.handleAssessmentSubmitRequest(asyncRequest, false, "token"));
    }

    @Test
    void testHandleAssessmentSubmitRequest_NormalFlow() throws Exception {
        Map<String, Object> asyncRequest = new HashMap<>();
        asyncRequest.put(Constants.USER_ID_CONSTANT, "user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        List<Map<String, Object>> children = new ArrayList<>();
        Map<String, Object> section = new HashMap<>();
        section.put(Constants.IDENTIFIER, "section1");
        section.put(Constants.CHILDREN, new ArrayList<>());
        children.add(section);
        submitRequest.put(Constants.CHILDREN, children);
        asyncRequest.put(Constants.REQUEST, submitRequest);

        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put(Constants.CHILDREN, children);
        hierarchy.put(Constants.SCORE_CUTOFF_TYPE, Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF);
        hierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(hierarchy);

        Map<String, Object> existingData = new HashMap<>();
        existingData.put(Constants.STATUS, "IN_PROGRESS");
        existingData.put(Constants.ASSESSMENT_READ_RESPONSE_KEY, "{\"children\":[{\"identifier\":\"section1\",\"childNodes\":[\"q1\"]}]}");
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(List.of(existingData));

        when(mapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Map.of("children", List.of(Map.of(Constants.IDENTIFIER, "section1", Constants.CHILD_NODES, List.of("q1")))));

        when(assessUtilServ.validateQumlAssessment(anyList(), anyList(), anyMap()))
                .thenReturn(Map.of("result", 100.0));
        when(assessUtilServ.readQListfromCache(anyList(), anyString(), anyBoolean(), anyString()))
                .thenReturn(Map.of());

        assertDoesNotThrow(() -> service.handleAssessmentSubmitRequest(asyncRequest, false, "token"));
    }

    @Test
    void testValidateQuestionListAPI_Negative() throws Exception {
        // Inject mocks
        ReflectionTestUtils.setField(service, "accessTokenValidator", accessTokenValidator);
        ReflectionTestUtils.setField(service, "assessUtilServ", assessUtilServ);
        ReflectionTestUtils.setField(service, "mapper", mapper);

        String token = "token";
        String userId = "user1";
        String assessmentId = "assess1";
        List<String> identifierList = new ArrayList<>();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.ASSESSMENT_ID_KEY, assessmentId);

        // Mock getQuestionIdList to return identifiers
        List<String> questionIds = Arrays.asList("q1", "q2");
        AssessmentServiceV4Impl spyService = Mockito.spy(service);

        // Mock dependencies
        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        Map<String, Object> assessmentDetail = new HashMap<>();
        assessmentDetail.put(Constants.PRIMARY_CATEGORY, "Assessment");
        when(assessUtilServ.readAssessmentHierarchyFromCache(assessmentId, false, token)).thenReturn(assessmentDetail);

        Map<String, Object> userAssessmentDetail = new HashMap<>();
        userAssessmentDetail.put(Constants.PRIMARY_CATEGORY, "Assessment");
        Map<String, Object> section = new HashMap<>();
        section.put(Constants.CHILD_NODES, questionIds);
        userAssessmentDetail.put(Constants.CHILDREN, List.of(section));
        String assessmentReadResponse = "{\"primaryCategory\":\"Assessment\",\"children\":[{\"childNodes\":[\"q1\",\"q2\"]}]}";
        List<Map<String, Object>> existingDataList = List.of(Map.of(Constants.ASSESSMENT_READ_RESPONSE_KEY, assessmentReadResponse));
        when(assessUtilServ.readUserSubmittedAssessmentRecords(userId, assessmentId)).thenReturn(existingDataList);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(userAssessmentDetail);

        // Call private method via reflection
        Method method = AssessmentServiceV4Impl.class.getDeclaredMethod(
                "validateQuestionListAPI", Map.class, String.class, List.class, boolean.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) method.invoke(spyService, requestBody, token, identifierList, false);

        assertEquals(Constants.IDENTIFIER_LIST_IS_EMPTY, result.get(Constants.ERROR_MESSAGE));
    }

    @Test
    void testValidateQuestionListAPI_BlankUserId() throws Exception {
        ReflectionTestUtils.setField(service, "accessTokenValidator", accessTokenValidator);

        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");

        Method method = AssessmentServiceV4Impl.class.getDeclaredMethod(
                "validateQuestionListAPI", Map.class, String.class, List.class, boolean.class);
        method.setAccessible(true);

        Map<String, Object> requestBody = new HashMap<>();
        List<String> identifierList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) method.invoke(service, requestBody, "token", identifierList, false);

        assertEquals(Constants.USER_ID_DOESNT_EXIST, result.get(Constants.ERROR_MESSAGE));
    }

    @Test
    void testValidateQuestionListAPI_EmptyAssessmentId() throws Exception {
        ReflectionTestUtils.setField(service, "accessTokenValidator", accessTokenValidator);

        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");

        Method method = AssessmentServiceV4Impl.class.getDeclaredMethod(
                "validateQuestionListAPI", Map.class, String.class, List.class, boolean.class);
        method.setAccessible(true);

        Map<String, Object> requestBody = new HashMap<>();
        List<String> identifierList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) method.invoke(service, requestBody, "token", identifierList, false);

        assertEquals(Constants.ASSESSMENT_ID_KEY_IS_NOT_PRESENT_IS_EMPTY, result.get(Constants.ERROR_MESSAGE));
    }

    @Test
    void testSubmitAssessmentAsync_Failed_InvalidAssessmentId() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> submitRequest = new HashMap<>();
        SBApiResponse resp = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
        assertEquals(Constants.INVALID_ASSESSMENT_ID, resp.getParams().getErrmsg());
    }

    @Test
    void testSubmitAssessmentAsync_Failed_ReadAssessment() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(Collections.emptyMap());
        SBApiResponse resp = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
        assertEquals(Constants.READ_ASSESSMENT_FAILED, resp.getParams().getErrmsg());
    }

    @Test
    void testSubmitAssessmentAsync_PracticeAssessment_NoLanguage() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, Constants.PRACTICE_QUESTION_SET);
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        assessmentHierarchy.put(Constants.SCORE_CUTOFF_TYPE, Constants.ASSESSMENT_LEVEL_SCORE_CUTOFF);
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(assessUtilServ.readAssessmentRecord(anyString(), anyList())).thenReturn("english");
        when(contentService.readContent(anyString())).thenReturn(Collections.emptyMap());
        when(assessUtilServ.validateQumlAssessment(anyList(), anyList(), anyMap())).thenReturn(new HashMap<>());
        SBApiResponse resp = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.SUCCESS, resp.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsync_SectionLevelScoreCutoff() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        List<Map<String, Object>> children = new ArrayList<>();
        Map<String, Object> section = new HashMap<>();
        section.put(Constants.IDENTIFIER, "section1");
        section.put(Constants.CHILDREN, new ArrayList<>());
        section.put(Constants.MINIMUM_PASS_PERCENTAGE, -1); //
        children.add(section);
        submitRequest.put(Constants.CHILDREN, children);

        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        assessmentHierarchy.put(Constants.CHILDREN, children);
        assessmentHierarchy.put(Constants.SCORE_CUTOFF_TYPE, Constants.SECTION_LEVEL_SCORE_CUTOFF);
        assessmentHierarchy.put(Constants.EXPECTED_DURATION, 10);
        Map<String, Object> existingData = new HashMap<>();
        existingData.put(Constants.START_TIME, new Date()); // Add start time
        existingData.put(Constants.STATUS, "IN_PROGRESS");
        existingData.put(Constants.ASSESSMENT_READ_RESPONSE_KEY, "{}");

        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenReturn(assessmentHierarchy);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(anyString(), anyString()))
                .thenReturn(Collections.singletonList(existingData));
        when(contentService.readContent(anyString())).thenReturn(Collections.emptyMap());
        when(assessUtilServ.validateQumlAssessment(anyList(), anyList(), anyMap())).thenReturn(new HashMap<>());

        SBApiResponse resp = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.SUCCESS, resp.getParams().getStatus());
    }

    @Test
    void testSubmitAssessmentAsync_AssessmentSubmitFailed() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        submitRequest.put(Constants.LANGUAGE, "english");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        when(assessUtilServ.readAssessmentHierarchyFromCache(anyString(), anyBoolean(), anyString()))
                .thenThrow(new RuntimeException("fail"));
        SBApiResponse resp = service.submitAssessmentAsync(submitRequest, "token", false);
        assertEquals(Constants.FAILED, resp.getParams().getStatus());
        assertTrue(resp.getParams().getErrmsg().contains("Failed to process assessment submit request"));
    }

    @Test
    void testReadAssessmentResultV4_Positive_Submitted() throws Exception {
        ReflectionTestUtils.setField(service, "accessTokenValidator", accessTokenValidator);
        ReflectionTestUtils.setField(service, "assessUtilServ", assessUtilServ);
        ReflectionTestUtils.setField(service, "mapper", mapper);

        String token = "token";
        String userId = "user1";
        String assessmentId = "assess1";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.ASSESSMENT_ID_KEY, assessmentId);
        requestBody.put("batchId", "batch1");      // Add mandatory field
        requestBody.put("courseId", "course1");
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.REQUEST, requestBody);

        Map<String, Object> submittedData = new HashMap<>();
        submittedData.put(Constants.STATUS, Constants.SUBMITTED);
        submittedData.put(Constants.SUBMIT_ASSESSMENT_RESPONSE_KEY, "{\"score\":100}");
        List<Map<String, Object>> existingDataList = List.of(submittedData);

        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(userId, assessmentId)).thenReturn(existingDataList);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(Map.of("score", 100));

        SBApiResponse resp = service.readAssessmentResultV4(request, token);
        assertEquals(100, resp.get("score"));
    }

    @Test
    void testReadAssessmentResultV4_Negative_BlankUserId() {
        ReflectionTestUtils.setField(service, "accessTokenValidator", accessTokenValidator);

        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("");

        SBApiResponse resp = service.readAssessmentResultV4(new HashMap<>(), "token");
        assertEquals(Constants.USER_ID_DOESNT_EXIST, resp.getParams().getErrmsg());
    }

    @Test
    void testReadAssessmentResultV4_Negative_InvalidRequest() {
        ReflectionTestUtils.setField(service, "accessTokenValidator", accessTokenValidator);

        when(accessTokenValidator.fetchUserIdFromAccessToken(anyString())).thenReturn("user1");

        Map<String, Object> request = new HashMap<>();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("batchId", "batch1");
        requestBody.put("courseId", "course1");
        request.put(Constants.REQUEST, requestBody);
        SBApiResponse resp = service.readAssessmentResultV4(request, "token");
        assertTrue(resp.getParams().getErrmsg().contains("One or more mandatory fields are missing in Request. Mandatory fields are : "));
    }

    @Test
    void testReadAssessmentResultV4_Negative_NoUserAssessmentData() {
        ReflectionTestUtils.setField(service, "accessTokenValidator", accessTokenValidator);
        ReflectionTestUtils.setField(service, "assessUtilServ", assessUtilServ);

        String token = "token";
        String userId = "user1";
        String assessmentId = "assess1";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.ASSESSMENT_ID_KEY, assessmentId);
        requestBody.put("batchId", "batch1");      // Add mandatory field
        requestBody.put("courseId", "course1");    // Add mandatory field
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.REQUEST, requestBody);

        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(userId, assessmentId)).thenReturn(Collections.emptyList());

        SBApiResponse resp = service.readAssessmentResultV4(request, token);
        assertEquals(Constants.USER_ASSESSMENT_DATA_NOT_PRESENT, resp.getParams().getErrmsg());
    }

    @Test
    void testReadAssessmentResultV4_StatusInProgress() {
        ReflectionTestUtils.setField(service, "accessTokenValidator", accessTokenValidator);
        ReflectionTestUtils.setField(service, "assessUtilServ", assessUtilServ);

        String token = "token";
        String userId = "user1";
        String assessmentId = "assess1";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.ASSESSMENT_ID_KEY, assessmentId);
        requestBody.put("batchId", "batch1");      // Add mandatory field
        requestBody.put("courseId", "course1");
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.REQUEST, requestBody);

        Map<String, Object> inProgressData = new HashMap<>();
        inProgressData.put(Constants.STATUS, "IN_PROGRESS");
        List<Map<String, Object>> existingDataList = List.of(inProgressData);

        when(accessTokenValidator.fetchUserIdFromAccessToken(token)).thenReturn(userId);
        when(assessUtilServ.readUserSubmittedAssessmentRecords(userId, assessmentId)).thenReturn(existingDataList);

        SBApiResponse resp = service.readAssessmentResultV4(request, token);
        assertTrue((Boolean) resp.getResult().get(Constants.STATUS_IS_IN_PROGRESS));
    }

}