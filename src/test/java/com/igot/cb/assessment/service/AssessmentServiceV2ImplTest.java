package com.igot.cb.assessment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.assessment.repo.AssessmentRepository;
import com.igot.cb.cache.RedisCacheMgr;
import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.util.AccessTokenValidator;
import com.igot.cb.common.util.CbExtAssessmentServerProperties;
import com.igot.cb.common.util.Constants;
import com.igot.cb.core.producer.Producer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.type.TypeReference;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AssessmentServiceV2ImplTest {

    @InjectMocks
    private AssessmentServiceV2Impl assessmentServiceV2;

    @Mock
    private RedisCacheMgr redisCacheMgr;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private AccessTokenValidator accessTokenValidator;

    @Mock
    private AssessmentUtilServiceV2 assessUtilServ;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private CbExtAssessmentServerProperties serverProperties;

    private static final String TOKEN = "dummyToken";
    private static final String ASSESSMENT_ID = "assess123";
    private static final String USER_ID = "user123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // +ve: readAssessment returns success
    @Test
    void testReadAssessment_success() throws JsonProcessingException {
        when(serverProperties.getAssessmentLevelParams()).thenReturn(Collections.emptyList());
        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenReturn(USER_ID);
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        assessmentHierarchy.put(Constants.EXPECTED_DURATION, 120);
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        String assessmentHierarchyJson = new ObjectMapper().writeValueAsString(assessmentHierarchy);
        when(redisCacheMgr.getCache(Constants.ASSESSMENT_ID + ASSESSMENT_ID)).thenReturn(assessmentHierarchyJson);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(assessmentHierarchy);
        when(assessmentRepository.fetchUserAssessmentDataFromDB(USER_ID, ASSESSMENT_ID)).thenReturn(new ArrayList<>());
        when(assessmentRepository.addUserAssesmentDataToDB(any(), any(), any(), any(), any(), any())).thenReturn(true);
        SBApiResponse response = assessmentServiceV2.readAssessment(ASSESSMENT_ID, TOKEN);
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
    }

    // -ve: readAssessment with invalid token
    @Test
    void testReadAssessment_invalidToken() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenReturn(null);
        SBApiResponse response = assessmentServiceV2.readAssessment(ASSESSMENT_ID, TOKEN);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains(Constants.USER_ID_DOESNT_EXIST));
    }

    // -ve: readAssessment throws exception
    @Test
    void testReadAssessment_exception() {
        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenThrow(new RuntimeException("DB error"));

        SBApiResponse response = assessmentServiceV2.readAssessment(ASSESSMENT_ID, TOKEN);

        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains("Failed to read Assessment"));
    }

    // +ve: submitAssessment returns success
    @Test
    void testSubmitAssessment_success() throws Exception {
        when(serverProperties.getUserAssessmentSubmissionDuration()).thenReturn("30");

        Map<String, Object> section = new HashMap<>();
        section.put(Constants.IDENTIFIER, "section1");
        section.put(Constants.CHILD_NODES, List.of("q1", "q2"));
        section.put(Constants.MINIMUM_PASS_PERCENTAGE, 50);
        section.put(Constants.OBJECT_TYPE, "Section");
        section.put(Constants.PRIMARY_CATEGORY, "Section");
        section.put(Constants.CHILDREN, new ArrayList<>()); // questions

        List<Map<String, Object>> children = List.of(section);

        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        assessmentHierarchy.put(Constants.EXPECTED_DURATION, 120);
        assessmentHierarchy.put(Constants.SCORE_CUTOFF_TYPE, "assessmentLevelScoreCutoff");
        assessmentHierarchy.put(Constants.CHILDREN, children);

        String assessmentHierarchyJson = new ObjectMapper().writeValueAsString(assessmentHierarchy);
        when(redisCacheMgr.getCache(Constants.ASSESSMENT_ID + ASSESSMENT_ID)).thenReturn(assessmentHierarchyJson);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(assessmentHierarchy);

        Map<String, Object> dbSection = new HashMap<>(section);
        dbSection.put(Constants.CHILD_NODES, List.of("q1", "q2"));
        List<Map<String, Object>> dbChildren = List.of(dbSection);

        Map<String, Object> dbAssessment = new HashMap<>(assessmentHierarchy);
        dbAssessment.put(Constants.CHILDREN, dbChildren);

        Map<String, Object> dbData = new HashMap<>();
        dbData.put(Constants.STATUS, Constants.NOT_SUBMITTED);
        dbData.put(Constants.START_TIME, new Date());
        dbData.put(Constants.ASSESSMENT_READ_RESPONSE_KEY, new ObjectMapper().writeValueAsString(dbAssessment));

        when(assessmentRepository.fetchUserAssessmentDataFromDB(USER_ID, ASSESSMENT_ID)).thenReturn(List.of(dbData));
        Map<String, Object> submitSection = new HashMap<>();
        submitSection.put(Constants.IDENTIFIER, "section1");
        submitSection.put(Constants.CHILDREN, new ArrayList<>()); // questions

        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, ASSESSMENT_ID);
        submitRequest.put(Constants.CHILDREN, List.of(submitSection));

        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenReturn(USER_ID);

        SBApiResponse response = assessmentServiceV2.submitAssessment(submitRequest, TOKEN, false);
        System.out.println("Error: " + response.getParams().getErrmsg());
        assertNotNull(response);
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
    }

    // -ve: submitAssessment with missing user
    @Test
    void testSubmitAssessment_userNotFound() throws Exception {
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, ASSESSMENT_ID);
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenReturn(null);

        SBApiResponse response = assessmentServiceV2.submitAssessment(submitRequest, TOKEN, false);

        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains(Constants.USER_ID_DOESNT_EXIST));
    }

    // -ve: submitAssessment with invalid assessment id
    @Test
    void testSubmitAssessment_invalidAssessmentId() throws Exception {
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "");
        submitRequest.put(Constants.CHILDREN, new ArrayList<>());
        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenReturn(USER_ID);

        SBApiResponse response = assessmentServiceV2.submitAssessment(submitRequest, TOKEN, false);

        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains(Constants.INVALID_ASSESSMENT_ID));
    }

    // +ve: readQuestionList returns success
    @Test
    void testReadQuestionList_success() throws JsonProcessingException {
        Map<String, Object> section = new HashMap<>();
        section.put(Constants.IDENTIFIER, "section1");
        section.put(Constants.CHILD_NODES, List.of("q1", "q2"));
        section.put(Constants.OBJECT_TYPE, "Section");
        section.put(Constants.PRIMARY_CATEGORY, "Section");
        section.put(Constants.CHILDREN, new ArrayList<>());

        List<Map<String, Object>> children = List.of(section);

        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.IDENTIFIER, ASSESSMENT_ID);
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        assessmentHierarchy.put(Constants.EXPECTED_DURATION, 120);
        assessmentHierarchy.put(Constants.CHILDREN, children);
        String assessmentHierarchyJson =
                "{\"identifier\":\"assess123\",\"primaryCategory\":\"Assessment\",\"children\":[{\"identifier\":\"section1\",\"primaryCategory\":\"Section\",\"children\":[],\"childNodes\":[\"q1\",\"q2\"],\"objectType\":\"Section\"}],\"expectedDuration\":120}";

        when(redisCacheMgr.getCache(Constants.ASSESSMENT_ID + ASSESSMENT_ID)).thenReturn(assessmentHierarchyJson);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(assessmentHierarchy);

        Map<String, Object> dbData = new HashMap<>();
        dbData.put(Constants.STATUS, Constants.NOT_SUBMITTED);
        dbData.put(Constants.START_TIME, new Date());
        dbData.put(Constants.ASSESSMENT_READ_RESPONSE, assessmentHierarchyJson);
        when(assessmentRepository.fetchUserAssessmentDataFromDB(USER_ID, ASSESSMENT_ID)).thenReturn(List.of(dbData));
        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenReturn(USER_ID);

        Map<String, Object> questionSet = new HashMap<>(assessmentHierarchy);
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.QUESTION_SET, questionSet);
        Map<String, Object> readHierarchyApiResponse = new HashMap<>();
        readHierarchyApiResponse.put(Constants.RESPONSE_CODE, Constants.OK);
        readHierarchyApiResponse.put(Constants.RESULT, result);

        Map<String, Object> search = new HashMap<>();
        search.put(Constants.IDENTIFIER, List.of("q1", "q2"));
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.SEARCH, search);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.REQUEST, request);
        requestBody.put(Constants.ASSESSMENT_ID_KEY, ASSESSMENT_ID);

        SBApiResponse response = assessmentServiceV2.readQuestionList(requestBody, TOKEN);

        assertNotNull(response);
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
    }

    // -ve: readQuestionList with missing assessment id
    @Test
    void testReadQuestionList_missingAssessmentId() {
        Map<String, Object> requestBody = new HashMap<>();
        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenReturn(USER_ID);

        SBApiResponse response = assessmentServiceV2.readQuestionList(requestBody, TOKEN);

        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains(Constants.ASSESSMENT_ID_KEY_IS_NOT_PRESENT_IS_EMPTY));
    }

    // -ve: readQuestionList with invalid token
    @Test
    void testReadQuestionList_invalidToken() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.ASSESSMENT_ID_KEY, ASSESSMENT_ID);
        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenReturn(null);

        SBApiResponse response = assessmentServiceV2.readQuestionList(requestBody, TOKEN);

        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains(Constants.USER_ID_DOESNT_EXIST));
    }

    // +ve: retakeAssessment returns success
    @Test
    void testRetakeAssessment_success() throws Exception {
        Map<String, Object> assessmentHierarchy = new HashMap<>();
        assessmentHierarchy.put(Constants.PRIMARY_CATEGORY, "Assessment");
        assessmentHierarchy.put(Constants.EXPECTED_DURATION, 120);
        assessmentHierarchy.put(Constants.CHILDREN, new ArrayList<>());
        String assessmentHierarchyJson = new ObjectMapper().writeValueAsString(assessmentHierarchy);

        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenReturn(USER_ID);
        when(assessmentRepository.fetchUserAssessmentDataFromDB(USER_ID, ASSESSMENT_ID)).thenReturn(new ArrayList<>());
        when(redisCacheMgr.getCache(Constants.ASSESSMENT_ID + ASSESSMENT_ID)).thenReturn(assessmentHierarchyJson);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(assessmentHierarchy);

        SBApiResponse response = assessmentServiceV2.retakeAssessment(ASSESSMENT_ID, TOKEN);

        assertNotNull(response);
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
    }

    // -ve: retakeAssessment with invalid token
    @Test
    void testRetakeAssessment_invalidToken() throws Exception {
        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenReturn(null);

        SBApiResponse response = assessmentServiceV2.retakeAssessment(ASSESSMENT_ID, TOKEN);

        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains(Constants.USER_ID_DOESNT_EXIST));
    }

    // -ve: retakeAssessment throws exception
    @Test
    void testRetakeAssessment_exception() throws Exception {
        when(accessTokenValidator.fetchUserIdFromAccessToken(TOKEN)).thenThrow(new RuntimeException("DB error"));

        SBApiResponse response = assessmentServiceV2.retakeAssessment(ASSESSMENT_ID, TOKEN);

        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertTrue(response.getParams().getErrmsg().contains("Failed to read Assessment"));
    }

    @Test
    void testCalculateSectionFinalResults() throws Exception {
        AssessmentServiceV2Impl service = new AssessmentServiceV2Impl();

        // Prepare dummy section results
        Map<String, Object> section1 = new HashMap<>();
        section1.put(Constants.RESULT, 80.0);
        section1.put(Constants.TOTAL, 10);
        section1.put(Constants.BLANK, 1);
        section1.put(Constants.CORRECT, 8);
        section1.put(Constants.INCORRECT, 1);
        section1.put(Constants.PASS_PERCENTAGE, 50);

        Map<String, Object> section2 = new HashMap<>();
        section2.put(Constants.RESULT, 70.0);
        section2.put(Constants.TOTAL, 10);
        section2.put(Constants.BLANK, 2);
        section2.put(Constants.CORRECT, 7);
        section2.put(Constants.INCORRECT, 1);
        section2.put(Constants.PASS_PERCENTAGE, 60);

        List<Map<String, Object>> sectionLevelResults = Arrays.asList(section1, section2);

        // Use reflection to access the private method
        Method method = AssessmentServiceV2Impl.class.getDeclaredMethod(
                "calculateSectionFinalResults", List.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(service, sectionLevelResults);

        assertNotNull(result);
        assertEquals(75.0, result.get(Constants.OVERALL_RESULT));
        assertEquals(20, result.get(Constants.TOTAL));
        assertEquals(3, result.get(Constants.BLANK));
        assertEquals(15, result.get(Constants.CORRECT));
        assertEquals(2, result.get(Constants.INCORRECT));
        assertEquals(true, result.get(Constants.PASS)); // Only one section passes
    }

    @Test
    void testWriteDataToDatabaseAndTriggerKafkaEvent() throws Exception {
        AssessmentServiceV2Impl service = new AssessmentServiceV2Impl();

        // Mock dependencies
        AssessmentRepository mockRepo = mock(AssessmentRepository.class);
        Producer mockProducer = mock(Producer.class);
        AssessmentUtilServiceV2 mockUtil = mock(AssessmentUtilServiceV2.class);
        CbExtAssessmentServerProperties mockProps = mock(CbExtAssessmentServerProperties.class);

        // Inject mocks
        Field repoField = AssessmentServiceV2Impl.class.getDeclaredField("assessmentRepository");
        repoField.setAccessible(true);
        repoField.set(service, mockRepo);

        Field producerField = AssessmentServiceV2Impl.class.getDeclaredField("kafkaProducer");
        producerField.setAccessible(true);
        producerField.set(service, mockProducer);

        Field utilField = AssessmentServiceV2Impl.class.getDeclaredField("assessUtilServ");
        utilField.setAccessible(true);
        utilField.set(service, mockUtil);

        Field propsField = AssessmentServiceV2Impl.class.getDeclaredField("serverProperties");
        propsField.setAccessible(true);
        propsField.set(service, mockProps);

        // Prepare input data
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.IDENTIFIER, "assess1");
        submitRequest.put(Constants.USER_ID, "user1");

        Map<String, Object> questionSetFromAssessment = new HashMap<>();
        questionSetFromAssessment.put(Constants.START_TIME, Instant.now());

        Map<String, Object> result = new HashMap<>();
        result.put(Constants.OVERALL_RESULT, 80.0);

        when(mockUtil.parseStartTimeToInstant(any())).thenReturn(Instant.now());
        when(mockRepo.updateUserAssesmentDataToDB(any(), any(), any(), any(), any(), any(), any())).thenReturn(true);
        when(mockProps.getAssessmentSubmitTopic()).thenReturn("topic");

        // Call private method via reflection
        Method method = AssessmentServiceV2Impl.class.getDeclaredMethod(
                "writeDataToDatabaseAndTriggerKafkaEvent",
                Map.class, String.class, Map.class, Map.class, String.class
        );
        method.setAccessible(true);
        method.invoke(service, submitRequest, "user1", questionSetFromAssessment, result, "Assessment");

        // Verify interactions
        verify(mockRepo, times(1)).updateUserAssesmentDataToDB(any(), any(), any(), any(), any(), any(), any());
        verify(mockProducer, times(1)).push(eq("topic"), any());
    }

    @Test
    void testCalculateAssessmentRetakeCount() throws Exception {
        AssessmentServiceV2Impl service = new AssessmentServiceV2Impl();

        Map<String, Object> entry1 = new HashMap<>();
        entry1.put(Constants.SUBMIT_ASSESSMENT_RESPONSE, "response1");
        Map<String, Object> entry2 = new HashMap<>();
        entry2.put(Constants.SUBMIT_ASSESSMENT_RESPONSE, null);
        Map<String, Object> entry3 = new HashMap<>();
        entry3.put(Constants.SUBMIT_ASSESSMENT_RESPONSE, "response2");

        List<Map<String, Object>> userAssessmentData = Arrays.asList(entry1, entry2, entry3);

        Method method = AssessmentServiceV2Impl.class.getDeclaredMethod(
                "calculateAssessmentRetakeCount", List.class);
        method.setAccessible(true);

        int count = (int) method.invoke(service, userAssessmentData);

        assertEquals(2, count);
    }

    @Test
    void testCreateResponseMapWithProperStructure_WithResultMap() {
        AssessmentServiceV2Impl service = new AssessmentServiceV2Impl();

        Map<String, Object> hierarchySection = new HashMap<>();
        hierarchySection.put(Constants.IDENTIFIER, "section1");
        hierarchySection.put(Constants.OBJECT_TYPE, "Section");
        hierarchySection.put(Constants.PRIMARY_CATEGORY, "Section");
        hierarchySection.put(Constants.MINIMUM_PASS_PERCENTAGE, 60);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(Constants.RESULT, 75.0);
        resultMap.put(Constants.TOTAL, 10);
        resultMap.put(Constants.BLANK, 2);
        resultMap.put(Constants.CORRECT, 7);
        resultMap.put(Constants.INCORRECT, 1);

        Map<String, Object> result = service.createResponseMapWithProperStructure(hierarchySection, resultMap);

        assertEquals("section1", result.get(Constants.IDENTIFIER));
        assertEquals("Section", result.get(Constants.OBJECT_TYPE));
        assertEquals("Section", result.get(Constants.PRIMARY_CATEGORY));
        assertEquals(60, result.get(Constants.PASS_PERCENTAGE));
        assertEquals(75.0, result.get(Constants.RESULT));
        assertEquals(10, result.get(Constants.TOTAL));
        assertEquals(2, result.get(Constants.BLANK));
        assertEquals(7, result.get(Constants.CORRECT));
        assertEquals(1, result.get(Constants.INCORRECT));
        assertEquals(true, result.get(Constants.PASS));
        assertEquals(75.0, result.get(Constants.OVERALL_RESULT));
    }

    @Test
    void testCreateResponseMapWithProperStructure_EmptyResultMap() {
        AssessmentServiceV2Impl service = new AssessmentServiceV2Impl();

        Map<String, Object> hierarchySection = new HashMap<>();
        hierarchySection.put(Constants.IDENTIFIER, "section2");
        hierarchySection.put(Constants.OBJECT_TYPE, "Section");
        hierarchySection.put(Constants.PRIMARY_CATEGORY, "Section");
        hierarchySection.put(Constants.MINIMUM_PASS_PERCENTAGE, 50);
        hierarchySection.put(Constants.CHILDREN, Arrays.asList("q1", "q2", "q3"));

        Map<String, Object> result = service.createResponseMapWithProperStructure(hierarchySection, null);

        assertEquals("section2", result.get(Constants.IDENTIFIER));
        assertEquals("Section", result.get(Constants.OBJECT_TYPE));
        assertEquals("Section", result.get(Constants.PRIMARY_CATEGORY));
        assertEquals(50, result.get(Constants.PASS_PERCENTAGE));
        assertEquals(0.0, result.get(Constants.RESULT));
        assertEquals(3, result.get(Constants.TOTAL));
        assertEquals(3, result.get(Constants.BLANK));
        assertEquals(0, result.get(Constants.CORRECT));
        assertEquals(0, result.get(Constants.INCORRECT));
        assertEquals(false, result.get(Constants.PASS));
        assertEquals(0.0, result.get(Constants.OVERALL_RESULT));
    }

    @Test
    void testCalculateAssessmentFinalResults() throws Exception {
        AssessmentServiceV2Impl service = new AssessmentServiceV2Impl();

        Map<String, Object> assessmentLevelResult = new HashMap<>();
        assessmentLevelResult.put(Constants.RESULT, 85.0);
        assessmentLevelResult.put(Constants.TOTAL, 20);
        assessmentLevelResult.put(Constants.BLANK, 2);
        assessmentLevelResult.put(Constants.CORRECT, 17);
        assessmentLevelResult.put(Constants.PASS_PERCENTAGE, 60);
        assessmentLevelResult.put(Constants.INCORRECT, 1);

        Method method = AssessmentServiceV2Impl.class.getDeclaredMethod(
                "calculateAssessmentFinalResults", Map.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(service, assessmentLevelResult);

        assertNotNull(result);
        assertEquals(85.0, result.get(Constants.OVERALL_RESULT));
        assertEquals(20, result.get(Constants.TOTAL));
        assertEquals(2, result.get(Constants.BLANK));
        assertEquals(17, result.get(Constants.CORRECT));
        assertEquals(60, result.get(Constants.PASS_PERCENTAGE));
        assertEquals(1, result.get(Constants.INCORRECT));
        assertEquals(true, result.get(Constants.PASS));
        assertTrue(result.get(Constants.CHILDREN) instanceof List);
        assertEquals(assessmentLevelResult, ((List<?>) result.get(Constants.CHILDREN)).get(0));
    }

    @Test
    void testReadSectionLevelParams_PopulatesSectionDetailsCorrectly() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        AssessmentServiceV2Impl service = new AssessmentServiceV2Impl();

        CbExtAssessmentServerProperties mockProps = mock(CbExtAssessmentServerProperties.class);
        List<String> sectionParams = List.of(Constants.IDENTIFIER, Constants.MINIMUM_PASS_PERCENTAGE, Constants.MAX_QUESTIONS);
        when(mockProps.getAssessmentSectionParams()).thenReturn(sectionParams);

        // Inject mock
        try {
            Field propsField = AssessmentServiceV2Impl.class.getDeclaredField("serverProperties");
            propsField.setAccessible(true);
            propsField.set(service, mockProps);
        } catch (Exception e) {
            fail("Failed to inject mock serverProperties");
        }

        // Prepare input assessmentAllDetail
        Map<String, Object> section1 = new HashMap<>();
        section1.put(Constants.IDENTIFIER, "section1");
        section1.put(Constants.MINIMUM_PASS_PERCENTAGE, 60);
        section1.put(Constants.MAX_QUESTIONS, 2);
        Map<String, Object> q1 = new HashMap<>();
        q1.put(Constants.IDENTIFIER, "q1");
        Map<String, Object> q2 = new HashMap<>();
        q2.put(Constants.IDENTIFIER, "q2");
        section1.put(Constants.CHILDREN, List.of(q1, q2));

        Map<String, Object> assessmentAllDetail = new HashMap<>();
        assessmentAllDetail.put(Constants.CHILDREN, List.of(section1));

        Map<String, Object> assessmentFilteredDetail = new HashMap<>();
        Method method = AssessmentServiceV2Impl.class.getDeclaredMethod(
                "readSectionLevelParams", Map.class, Map.class);
        method.setAccessible(true);
        method.invoke(service, assessmentAllDetail, assessmentFilteredDetail);


        // Assertions
        assertTrue(assessmentFilteredDetail.containsKey(Constants.CHILDREN));
        List<Map<String, Object>> sectionResponse = (List<Map<String, Object>>) assessmentFilteredDetail.get(Constants.CHILDREN);
        assertEquals(1, sectionResponse.size());
        Map<String, Object> newSection = sectionResponse.get(0);
        assertEquals("section1", newSection.get(Constants.IDENTIFIER));
        assertEquals(60, newSection.get(Constants.MINIMUM_PASS_PERCENTAGE));
        assertTrue(newSection.containsKey(Constants.CHILD_NODES));
        List<String> childNodes = (List<String>) newSection.get(Constants.CHILD_NODES);
        assertEquals(2, childNodes.size());
        assertTrue(childNodes.contains("q1") && childNodes.contains("q2"));

        assertTrue(assessmentFilteredDetail.containsKey(Constants.CHILD_NODES));
        List<String> sectionIdList = (List<String>) assessmentFilteredDetail.get(Constants.CHILD_NODES);
        assertEquals(List.of("section1"), sectionIdList);
    }

}
