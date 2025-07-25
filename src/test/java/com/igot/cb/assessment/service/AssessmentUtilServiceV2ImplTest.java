package com.igot.cb.assessment.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.cache.RedisCacheMgr;
import com.igot.cb.cassandra.utils.CassandraOperation;

import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.service.ContentService;
import com.igot.cb.common.service.OutboundRequestHandlerServiceImpl;
import com.igot.cb.common.util.CbExtAssessmentServerProperties;
import com.igot.cb.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssessmentUtilServiceV2ImplTest {

    @InjectMocks
    AssessmentUtilServiceV2Impl utilService;

    @Mock
    CbExtAssessmentServerProperties serverProperties;
    @Mock
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;
    @Mock
    ObjectMapper mapper;
    @Mock
    CassandraOperation cassandraOperation;
    @Mock
    RedisCacheMgr redisCacheMgr;
    @Mock
    ContentService contentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidateQumlAssessment_Positive() {
        List<String> originalQ = List.of("q1");
        Map<String, Object> qMap = new HashMap<>();
        Map<String, Object> q = new HashMap<>();
        q.put(Constants.QUESTION_TYPE, Constants.MCQ_SCA);
        Map<String, Object> editorState = new HashMap<>();
        Map<String, Object> opt = new HashMap<>();
        opt.put(Constants.INDEX, "1");
        opt.put(Constants.SELECTED_ANSWER, true);
        opt.put(Constants.ANSWER, true);
        editorState.put(Constants.OPTIONS, List.of(opt));
        q.put(Constants.EDITOR_STATE, editorState);
        q.put(Constants.IDENTIFIER, "q1");
        qMap.put("q1", q);

        List<Map<String, Object>> userQ = new ArrayList<>();
        Map<String, Object> userQ1 = new HashMap<>(q);
        userQ.add(userQ1);

        Map<String, Object> result = utilService.validateQumlAssessment(originalQ, userQ, qMap);
        assertNotNull(result);
        assertTrue(result.containsKey(Constants.RESULT));
    }

    @Test
    void testValidateQumlAssessment_NullInputs() {
        Map<String, Object> result = utilService.validateQumlAssessment(null, null, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- fetchQuestionIdentifierValue ---

    @Test
    void testFetchQuestionIdentifierValue_Positive() throws Exception {
        when(serverProperties.getAssessmentQuestionParams()).thenReturn(List.of(Constants.IDENTIFIER));
        when(outboundRequestHandlerService.fetchResultUsingPost(anyString(), anyMap(), anyMap()))
                .thenReturn(Map.of(
                        Constants.RESPONSE_CODE, Constants.OK,
                        Constants.RESULT, Map.of(
                                Constants.QUESTIONS, List.of(
                                        Map.of(Constants.IDENTIFIER, "q1")
                                )
                        )
                ));
        List<String> ids = List.of("q1");
        List<Object> qList = new ArrayList<>();
        String result = utilService.fetchQuestionIdentifierValue(ids, qList, "category");
        assertEquals("", result);
        assertTrue(qList.isEmpty());
    }

    @Test
    void testFetchQuestionIdentifierValue_EmptyIds() throws Exception {
        List<Object> qList = new ArrayList<>();
        String result = utilService.fetchQuestionIdentifierValue(Collections.emptyList(), qList, "cat");
        assertEquals("", result);
        assertTrue(qList.isEmpty());
    }

    // --- filterQuestionMapDetail ---

    @Test
    void testFilterQuestionMapDetail_Positive() {
        Map<String, Object> qMap = new HashMap<>();
        qMap.put(Constants.IDENTIFIER, "q1");
        when(serverProperties.getAssessmentQuestionParams()).thenReturn(List.of(Constants.IDENTIFIER));
        Map<String, Object> result = utilService.filterQuestionMapDetail(qMap, Constants.PRACTICE_QUESTION_SET);
        assertEquals("q1", result.get(Constants.IDENTIFIER));
    }

    @Test
    void testFilterQuestionMapDetail_MissingParams() {
        Map<String, Object> qMap = new HashMap<>();
        when(serverProperties.getAssessmentQuestionParams()).thenReturn(List.of("nonexistent"));
        Map<String, Object> result = utilService.filterQuestionMapDetail(qMap, "cat");
        assertTrue(result.isEmpty());
    }

    // --- readQuestionDetails ---

    @Test
    void testReadQuestionDetails_Positive() {
        when(serverProperties.getAssessmentHost()).thenReturn("http://host/");
        when(serverProperties.getAssessmentQuestionListPath()).thenReturn("path");
        when(serverProperties.getSbApiKey()).thenReturn("key");
        when(outboundRequestHandlerService.fetchResultUsingPost(anyString(), anyMap(), anyMap()))
                .thenReturn(Map.of(Constants.RESULT, Map.of(Constants.QUESTIONS, List.of(Map.of(Constants.IDENTIFIER, "q1")))));
        List<Map<String, Object>> result = utilService.readQuestionDetails(List.of("q1"));
        assertFalse(result.isEmpty());
    }

    @Test
    void testReadQuestionDetails_Exception() {
        when(serverProperties.getAssessmentHost()).thenThrow(new RuntimeException("fail"));
        List<Map<String, Object>> result = utilService.readQuestionDetails(List.of("q1"));
        assertTrue(result.isEmpty());
    }

    // --- getReadHierarchyApiResponse ---

    @Test
    void testGetReadHierarchyApiResponse_Positive() {
        when(serverProperties.getAssessmentHost()).thenReturn("http://host/");
        when(serverProperties.getAssessmentHierarchyReadPath()).thenReturn("read/{id}");
        when(serverProperties.getSbApiKey()).thenReturn("key");
        when(outboundRequestHandlerService.fetchUsingGetWithHeaders(anyString(), anyMap()))
                .thenReturn(Map.of(Constants.RESULT, Map.of(Constants.QUESTION_SET, Map.of("id", "qset"))));
        when(mapper.convertValue(any(), eq(Map.class))).thenReturn(Map.of(Constants.RESULT, Map.of(Constants.QUESTION_SET, Map.of("id", "qset"))));
        Map<String, Object> result = utilService.getReadHierarchyApiResponse("id", "token");
        assertTrue(result.containsKey(Constants.RESULT));
    }

    @Test
    void testGetReadHierarchyApiResponse_Exception() {
        when(serverProperties.getAssessmentHost()).thenThrow(new RuntimeException("fail"));
        Map<String, Object> result = utilService.getReadHierarchyApiResponse("id", "token");
        assertTrue(result.isEmpty());
    }

    // --- parseStartTimeToInstant ---

    @Test
    void testParseStartTimeToInstant_Long() {
        Instant now = Instant.now();
        Instant result = utilService.parseStartTimeToInstant(now.toEpochMilli());
        assertEquals(now.getEpochSecond(), result.getEpochSecond(), 1);
    }

    @Test
    void testParseStartTimeToInstant_StringEpoch() {
        Instant now = Instant.now();
        Instant result = utilService.parseStartTimeToInstant(String.valueOf(now.toEpochMilli()));
        assertEquals(now.getEpochSecond(), result.getEpochSecond(), 1);
    }

    @Test
    void testParseStartTimeToInstant_ISO8601() {
        Instant now = Instant.now();
        Instant result = utilService.parseStartTimeToInstant(now.toString());
        assertEquals(now.getEpochSecond(), result.getEpochSecond(), 1);
    }

    @Test
    void testParseStartTimeToInstant_Instant() {
        Instant now = Instant.now();
        Instant result = utilService.parseStartTimeToInstant(now);
        assertEquals(now, result);
    }

    @Test
    void testParseStartTimeToInstant_Date() {
        Date date = new Date();
        Instant result = utilService.parseStartTimeToInstant(date);
        assertEquals(date.toInstant(), result);
    }

    @Test
    void testParseStartTimeToInstant_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> utilService.parseStartTimeToInstant(new Object()));
    }

    // --- parseStartTimeToLong ---

    @Test
    void testParseStartTimeToLong_Date() {
        Date date = new Date();
        Long result = utilService.parseStartTimeToLong(date);
        assertEquals(date.getTime(), result);
    }

    @Test
    void testParseStartTimeToLong_Instant() {
        Instant now = Instant.now();
        Long result = utilService.parseStartTimeToLong(now);
        assertEquals(now.toEpochMilli(), result);
    }

    @Test
    void testParseStartTimeToLong_Long() {
        Long now = System.currentTimeMillis();
        Long result = utilService.parseStartTimeToLong(now);
        assertEquals(now, result);
    }

    @Test
    void testParseStartTimeToLong_StringEpoch() {
        Long now = System.currentTimeMillis();
        Long result = utilService.parseStartTimeToLong(String.valueOf(now));
        assertEquals(now, result);
    }

    @Test
    void testParseStartTimeToLong_ISO8601() {
        Instant now = Instant.now();
        Long result = utilService.parseStartTimeToLong(now.toString());
        assertEquals(now.toEpochMilli(), result);
    }

    @Test
    void testParseStartTimeToLong_Invalid() {
        assertEquals(0L, utilService.parseStartTimeToLong(new Object()));
    }

    @Test
    void testReadAssessmentHierarchyFromCache_FromRedis() throws Exception {
        String assessmentId = "aid";
        String cacheKey = Constants.ASSESSMENT_ID + assessmentId + Constants.UNDER_SCORE + Constants.QUESTION_SET;
        Map<String, Object> expected = Map.of("foo", "bar");
        String json = new ObjectMapper().writeValueAsString(expected);

        when(serverProperties.qListFromCacheEnabled()).thenReturn(true);
        when(redisCacheMgr.getCache(cacheKey)).thenReturn(json);
        when(mapper.readValue(eq(json), any(TypeReference.class))).thenReturn(expected);

        Map<String, Object> result = utilService.readAssessmentHierarchyFromCache(assessmentId, false, "token");
        assertEquals(expected, result);
    }

    @Test
    void testReadAssessmentHierarchyFromCache_FromCassandra() throws Exception {
        String assessmentId = "aid";
        String cacheKey = Constants.ASSESSMENT_ID + assessmentId + Constants.UNDER_SCORE + Constants.QUESTION_SET;
        when(serverProperties.qListFromCacheEnabled()).thenReturn(true);
        when(redisCacheMgr.getCache(cacheKey)).thenReturn("");
        Map<String, Object> dbEntry = new HashMap<>();
        dbEntry.put(Constants.HIERARCHY, "{\"foo\":\"bar\"}");
        List<Map<String, Object>> dbList = List.of(dbEntry);
        when(cassandraOperation.getRecordsByPropertiesWithoutFiltering(any(), any(), any(), any())).thenReturn(dbList);
        Map<String, Object> expected = Map.of("foo", "bar");
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(expected);

        Map<String, Object> result = utilService.readAssessmentHierarchyFromCache(assessmentId, false, "token");
        assertEquals(expected, result);
    }

// --- fetchWheebox ---

    @Test
    void testFetchWheebox_FromRedis() throws Exception {
        String userId = "user1";
        String key = "wheebox_user1";
        Map<String, Object> expected = Map.of("score", 99);
        String json = new ObjectMapper().writeValueAsString(expected);

        when(serverProperties.getRedisWheeboxKey()).thenReturn("wheebox");
        when(redisCacheMgr.getContentFromCache(key)).thenReturn(json);
        when(mapper.readValue(eq(json), any(TypeReference.class))).thenReturn(expected);

        Map<String, Object> result = utilService.fetchWheebox(userId);
        assertEquals(expected, result);
    }

    @Test
    void testFetchWheebox_EmptyRedis() {
        when(serverProperties.getRedisWheeboxKey()).thenReturn("wheebox");
        when(redisCacheMgr.getContentFromCache(anyString())).thenReturn(null);

        Map<String, Object> result = utilService.fetchWheebox("user1");
        assertTrue(result.isEmpty());
    }

    @Test
    void testValidateContextLocking_ContentNotFound() {
        Map<String, Object> assessmentAllDetail = new HashMap<>();
        assessmentAllDetail.put(Constants.CONTEXT_CATEGORY_TAG, Constants.FINAL_PROGRAM_ASSESSMENT);
        String parentContextId = "parent1";
        SBApiResponse response = new SBApiResponse();
        String userId = "user1";

        when(contentService.readContentFromCache(parentContextId, null)).thenReturn(Collections.emptyMap());

        String result = utilService.validateContextLocking(assessmentAllDetail, parentContextId, response, userId);
        assertEquals(Constants.CONTENT_NOT_FOUND, result);
    }


    @Test
    void testReadAssessmentRecord_Positive() {
        String assessmentId = "aid";
        List<String> fields = List.of("language");
        Map<String, Object> content = new HashMap<>();
        content.put(Constants.LANGUAGE, List.of("en"));
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(Constants.CONTENT, content);
        Map<String, Object> response = new HashMap<>();
        response.put(Constants.RESULT, resultMap);

        when(serverProperties.getContentHost()).thenReturn("http://host/");
        when(serverProperties.getCourseReadPath()).thenReturn("read/");
        when(outboundRequestHandlerService.fetchResultUsingGet(anyString(), anyMap())).thenReturn(response);

        String lang = utilService.readAssessmentRecord(assessmentId, fields);
        assertEquals("en", lang);
    }

    @Test
    void testReadAssessmentRecord_NoResult() {
        String assessmentId = "aid";
        List<String> fields = List.of("language");
        when(serverProperties.getContentHost()).thenReturn("http://host/");
        when(serverProperties.getCourseReadPath()).thenReturn("read/");
        when(outboundRequestHandlerService.fetchResultUsingGet(anyString(), anyMap())).thenReturn(Collections.emptyMap());

        String lang = utilService.readAssessmentRecord(assessmentId, fields);
        assertEquals("", lang);
    }

    @Test
    void testValidateQumlAssessmentV2_CorrectAndBlank() {
        // Setup question set details
        Map<String, Object> questionSetDetailsMap = new HashMap<>();
        questionSetDetailsMap.put(Constants.ASSESSMENT_TYPE, Constants.QUESTION_WEIGHTAGE);
        questionSetDetailsMap.put(Constants.MINIMUM_PASS_PERCENTAGE, 50);
        questionSetDetailsMap.put(Constants.TOTAL_MARKS, 10);
        Map<String, Object> sectionScheme = new HashMap<>();
        sectionScheme.put("EASY", 10);
        questionSetDetailsMap.put(Constants.QUESTION_SECTION_SCHEME, sectionScheme);
        questionSetDetailsMap.put(Constants.NEGATIVE_MARKING_PERCENTAGE, "0%");

        // Original question list
        List<String> originalQuestionList = List.of("q1", "q2");

        // Question map
        Map<String, Object> questionMap = new HashMap<>();
        Map<String, Object> q1 = new HashMap<>();
        q1.put(Constants.IDENTIFIER, "q1");
        q1.put(Constants.QUESTION_TYPE, Constants.MCQ_SCA);
        q1.put(Constants.EDITOR_STATE, Map.of(Constants.OPTIONS, List.of(
                Map.of(Constants.INDEX, "1", Constants.SELECTED_ANSWER, true, Constants.ANSWER, true)
        )));
        q1.put(Constants.QUESTION_LEVEL, "EASY");
        questionMap.put("q1", q1);

        Map<String, Object> q2 = new HashMap<>();
        q2.put(Constants.IDENTIFIER, "q2");
        q2.put(Constants.QUESTION_TYPE, Constants.MCQ_SCA);
        q2.put(Constants.EDITOR_STATE, Map.of(Constants.OPTIONS, List.of(
                Map.of(Constants.INDEX, "1", Constants.SELECTED_ANSWER, false, Constants.ANSWER, true)
        )));
        q2.put(Constants.QUESTION_LEVEL, "EASY");
        questionMap.put("q2", q2);

        // User question list: q1 answered, q2 blank
        Map<String, Object> userQ1 = new HashMap<>(q1);
        userQ1.put(Constants.RESULT, Constants.CORRECT);
        List<Map<String, Object>> userQuestionList = new ArrayList<>();
        userQuestionList.add(userQ1);
        Map<String, Object> userQ2 = new HashMap<>(q2); // blank, no result
        userQuestionList.add(userQ1);
        userQuestionList.add(userQ2);
        // Call method
        Map<String, Object> result = utilService.validateQumlAssessmentV2(
                questionSetDetailsMap, originalQuestionList, userQuestionList, questionMap);

        assertNotNull(result);
        assertEquals(0, result.get(Constants.CORRECT));
        assertEquals(1, result.get(Constants.BLANK));
        assertEquals(2, result.get(Constants.INCORRECT));
        assertEquals(0.0, result.get(Constants.SECTION_MARKS));
        assertEquals(10, result.get(Constants.TOTAL_MARKS));
        assertEquals(Constants.FAIL, result.get(Constants.SECTION_RESULT));
    }

    @Test
    void testGetQumlAnswersV2_MCQ_SCA() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String qid = "q1";
        Map<String, Object> questionMap = new HashMap<>();
        Map<String, Object> question = new HashMap<>();
        question.put(Constants.IDENTIFIER, qid);
        question.put(Constants.QUESTION_TYPE, Constants.MCQ_SCA);
        Map<String, Object> editorState = new HashMap<>();
        List<Map<String, Object>> options = new ArrayList<>();
        Map<String, Object> option = new HashMap<>();
        option.put(Constants.ANSWER, true);
        Map<String, Object> valueObj = new HashMap<>();
        valueObj.put(Constants.VALUE, "A");
        option.put(Constants.VALUE, valueObj);
        options.add(option);
        editorState.put(Constants.OPTIONS, options);
        question.put(Constants.EDITOR_STATE, editorState);
        questionMap.put(qid, question);

        // Mock mapper behavior
        when(mapper.convertValue(any(), any(TypeReference.class)))
                .thenReturn(question)
                .thenReturn(editorState)
                .thenReturn(options)
                .thenReturn(valueObj);

        List<String> questions = List.of(qid);
        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswersV2", List.class, Map.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, questions, questionMap);

        assertNotNull(result);
        assertTrue(result.containsKey(qid));
        assertEquals(List.of("A"), result.get(qid));
    }

    @Test
    void testIsAllCourseCompleted_AllCompleted() {
        ReflectionTestUtils.setField(utilService, "cassandraOperation", cassandraOperation);

        String userId = "user1";
        List<String> courseIds = List.of("courseA", "courseB");
        Map<String, Object> enrolment1 = new HashMap<>();
        enrolment1.put(Constants.STATUS, Constants.ASSESSMENT_STATUS_COMPLETED);
        enrolment1.put(Constants.COURSE_ID, "courseA");
        Map<String, Object> enrolment2 = new HashMap<>();
        enrolment2.put(Constants.STATUS, Constants.ASSESSMENT_STATUS_COMPLETED);
        enrolment2.put(Constants.COURSE_ID, "courseB");

        when(cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                anyString(), anyString(), anyMap(), anyList()))
                .thenReturn(List.of(enrolment1, enrolment2));

        boolean result = ReflectionTestUtils.invokeMethod(utilService, "isAllCourseCompleted", userId, courseIds);
        assertTrue(result);
    }

    @Test
    void testIsAllCourseCompleted_NotAllCompleted() {
        ReflectionTestUtils.setField(utilService, "cassandraOperation", cassandraOperation);

        String userId = "user1";
        List<String> courseIds = List.of("courseA", "courseB");
        Map<String, Object> enrolment1 = new HashMap<>();
        enrolment1.put(Constants.STATUS, Constants.ASSESSMENT_STATUS_COMPLETED);
        enrolment1.put(Constants.COURSE_ID, "courseA");
        Map<String, Object> enrolment2 = new HashMap<>();
        enrolment2.put(Constants.STATUS, 0); // Not completed
        enrolment2.put(Constants.COURSE_ID, "courseB");

        when(cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                anyString(), anyString(), anyMap(), anyList()))
                .thenReturn(List.of(enrolment1, enrolment2));

        boolean result = ReflectionTestUtils.invokeMethod(utilService, "isAllCourseCompleted", userId, courseIds);
        assertFalse(result);
    }

    @Test
    void testIsAllCourseCompleted_EmptyCourseIds() {
        boolean result = ReflectionTestUtils.invokeMethod(utilService, "isAllCourseCompleted", "user1", Collections.emptyList());
        assertFalse(result);
    }

    @Test
    void testReadAssessmentRecord_Exception() {
        String assessmentIdentifier = "assess1";
        List<String> fields = List.of("field1", "field2");

        when(outboundRequestHandlerService.fetchResultUsingGet(anyString(), anyMap()))
                .thenThrow(new RuntimeException("Service error"));

        String result = utilService.readAssessmentRecord(assessmentIdentifier, fields);

        assertEquals("", result);
    }

    @Test
    void testSortAnswers_MultipleElements() throws Exception {
        List<String> answers = new ArrayList<>(Arrays.asList("C", "A", "B"));
        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("sortAnswers", List.class);
        method.setAccessible(true);
        method.invoke(utilService, answers);
        assertEquals(Arrays.asList("A", "B", "C"), answers);
    }

    @Test
    void testSortAnswers_SingleElement() throws Exception {
        List<String> answers = new ArrayList<>(Collections.singletonList("A"));
        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("sortAnswers", List.class);
        method.setAccessible(true);
        method.invoke(utilService, answers);
        assertEquals(Collections.singletonList("A"), answers);
    }

    @Test
    void testSortAnswers_EmptyList() throws Exception {
        List<String> answers = new ArrayList<>();
        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("sortAnswers", List.class);
        method.setAccessible(true);
        method.invoke(utilService, answers);
        assertTrue(answers.isEmpty());
    }

    @Test
    void testValidateQumlAssessment_CorrectAnswer() {
        List<String> originalQuestionList = List.of("q1");
        Map<String, Object> questionMap = new HashMap<>();
        Map<String, Object> question = new HashMap<>();
        question.put(Constants.IDENTIFIER, "q1");
        question.put(Constants.QUESTION_TYPE, Constants.MCQ_SCA);
        Map<String, Object> editorState = new HashMap<>();
        Map<String, Object> option = new HashMap<>();
        option.put(Constants.INDEX, "1");
        option.put(Constants.SELECTED_ANSWER, true);
        option.put(Constants.ANSWER, true);
        editorState.put(Constants.OPTIONS, List.of(option));
        question.put(Constants.EDITOR_STATE, editorState);
        questionMap.put("q1", question);

        List<Map<String, Object>> userQuestionList = new ArrayList<>();
        Map<String, Object> userQuestion = new HashMap<>(question);
        userQuestionList.add(userQuestion);

        Map<String, Object> result = utilService.validateQumlAssessment(originalQuestionList, userQuestionList, questionMap);

        assertNotNull(result);
        assertTrue(result.containsKey(Constants.RESULT));
        assertTrue(result.containsKey(Constants.CORRECT));
        assertTrue(result.containsKey(Constants.INCORRECT));
        assertTrue(result.containsKey(Constants.BLANK));
    }

    @Test
    void testValidateQumlAssessment_IncorrectAnswer() {
        List<String> originalQuestionList = List.of("q1");
        Map<String, Object> questionMap = new HashMap<>();
        Map<String, Object> question = new HashMap<>();
        question.put(Constants.IDENTIFIER, "q1");
        question.put(Constants.QUESTION_TYPE, Constants.MCQ_SCA);
        Map<String, Object> editorState = new HashMap<>();
        Map<String, Object> option = new HashMap<>();
        option.put(Constants.INDEX, "1");
        option.put(Constants.SELECTED_ANSWER, true);
        option.put(Constants.ANSWER, false);
        editorState.put(Constants.OPTIONS, List.of(option));
        question.put(Constants.EDITOR_STATE, editorState);
        questionMap.put("q1", question);

        List<Map<String, Object>> userQuestionList = new ArrayList<>();
        Map<String, Object> userQuestion = new HashMap<>(question);
        userQuestionList.add(userQuestion);

        Map<String, Object> result = utilService.validateQumlAssessment(originalQuestionList, userQuestionList, questionMap);

        assertNotNull(result);
        assertTrue(result.containsKey(Constants.INCORRECT));
        assertTrue((Integer) result.get(Constants.INCORRECT) > 0);
    }

    @Test
    void testValidateQumlAssessment_Exception() {
        // Pass invalid input to trigger catch block
        Map<String, Object> result = utilService.validateQumlAssessment(null, null, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleBlankAnswers_NoBlank() throws Exception {
        List<Map<String, Object>> userQuestionList = Arrays.asList(new HashMap<>(), new HashMap<>());
        Map<String, Object> answers = new HashMap<>();
        answers.put("q1", "A");
        answers.put("q2", "B");
        Integer blank = 0;

        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("handleBlankAnswers", List.class, Map.class, Integer.class);
        method.setAccessible(true);
        Integer result = (Integer) method.invoke(utilService, userQuestionList, answers, blank);

        assertEquals(0, result);
    }

    @Test
    void testHandleBlankAnswers_WithBlank() throws Exception {
        List<Map<String, Object>> userQuestionList = List.of(new HashMap<>());
        Map<String, Object> answers = new HashMap<>();
        answers.put("q1", "A");
        answers.put("q2", "B");
        Integer blank = 0;

        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("handleBlankAnswers", List.class, Map.class, Integer.class);
        method.setAccessible(true);
        Integer result = (Integer) method.invoke(utilService, userQuestionList, answers, blank);

        assertEquals(1, result);
    }

    @Test
    void testHandleBlankAnswers_EmptyAnswers() throws Exception {
        List<Map<String, Object>> userQuestionList = List.of(new HashMap<>());
        Map<String, Object> answers = new HashMap<>();
        Integer blank = 0;

        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("handleBlankAnswers", List.class, Map.class, Integer.class);
        method.setAccessible(true);
        Integer result = (Integer) method.invoke(utilService, userQuestionList, answers, blank);

        assertEquals(0, result);
    }

    @Test
    void testHandleBlankAnswers_EmptyUserQuestions() throws Exception {
        List<Map<String, Object>> userQuestionList = new ArrayList<>();
        Map<String, Object> answers = new HashMap<>();
        answers.put("q1", "A");
        answers.put("q2", "B");
        Integer blank = 0;

        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("handleBlankAnswers", List.class, Map.class, Integer.class);
        method.setAccessible(true);
        Integer result = (Integer) method.invoke(utilService, userQuestionList, answers, blank);

        assertEquals(2, result);
    }

    // MCQ_MCA: Multiple correct answers
    @Test
    void testGetQumlAnswersV2_MCQ_MCA_MultipleCorrect() throws Exception {
        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswersV2", List.class, Map.class);
        method.setAccessible(true);

        Map<String, Object> mcqMca = new HashMap<>();
        mcqMca.put(Constants.IDENTIFIER, "q2");
        mcqMca.put(Constants.QUESTION_TYPE, Constants.MCQ_MCA);
        Map<String, Object> editorStateMca = new HashMap<>();
        Map<String, Object> optionMca1 = new HashMap<>();
        optionMca1.put(Constants.ANSWER, true);
        optionMca1.put(Constants.VALUE, Map.of(Constants.VALUE, "B"));
        Map<String, Object> optionMca2 = new HashMap<>();
        optionMca2.put(Constants.ANSWER, true);
        optionMca2.put(Constants.VALUE, Map.of(Constants.VALUE, "C"));
        editorStateMca.put(Constants.OPTIONS, List.of(optionMca1, optionMca2));
        mcqMca.put(Constants.EDITOR_STATE, editorStateMca);

        Map<String, Object> questionMap = Map.of("q2", mcqMca);
        List<String> questions = List.of("q2");

        when(utilService.mapper.convertValue(any(), any(TypeReference.class))).thenAnswer(invocation -> invocation.getArgument(0));
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, questions, questionMap);

        assertNotNull(result);
        assertEquals(List.of("B", "C"), result.get("q2"));
    }

    // FTB: Fill the blank with position
    @Test
    void testGetQumlAnswersV2_FTB() throws Exception {
        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswersV2", List.class, Map.class);
        method.setAccessible(true);

        Map<String, Object> ftb = new HashMap<>();
        ftb.put(Constants.IDENTIFIER, "q3");
        ftb.put(Constants.QUESTION_TYPE, Constants.FTB);
        Map<String, Object> editorStateFtb = new HashMap<>();
        Map<String, Object> optionFtb = new HashMap<>();
        optionFtb.put(Constants.ANSWER, true);
        optionFtb.put("position", "2");
        optionFtb.put(Constants.VALUE, Map.of(Constants.BODY, "D"));
        editorStateFtb.put(Constants.OPTIONS, List.of(optionFtb));
        ftb.put(Constants.EDITOR_STATE, editorStateFtb);

        Map<String, Object> questionMap = Map.of("q3", ftb);
        List<String> questions = List.of("q3");

        when(utilService.mapper.convertValue(any(), any(TypeReference.class))).thenAnswer(invocation -> invocation.getArgument(0));
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, questions, questionMap);

        assertNotNull(result);
        assertEquals(List.of("1-D"), result.get("q3")); // position-1 (2-1)
    }

    // MTF: Match the following
    @Test
    void testGetQumlAnswersV2_MTF() throws Exception {
        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswersV2", List.class, Map.class);
        method.setAccessible(true);

        Map<String, Object> mtf = new HashMap<>();
        mtf.put(Constants.IDENTIFIER, "q4");
        mtf.put(Constants.QUESTION_TYPE, Constants.MTF);
        Map<String, Object> editorStateMtf = new HashMap<>();
        Map<String, Object> optionMtf = new HashMap<>();
        optionMtf.put(Constants.ANSWER, true);
        optionMtf.put(Constants.VALUE, Map.of(Constants.VALUE, "E"));
        editorStateMtf.put(Constants.OPTIONS, List.of(optionMtf));
        mtf.put(Constants.EDITOR_STATE, editorStateMtf);

        Map<String, Object> questionMap = Map.of("q4", mtf);
        List<String> questions = List.of("q4");

        when(utilService.mapper.convertValue(any(), any(TypeReference.class))).thenAnswer(invocation -> invocation.getArgument(0));
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, questions, questionMap);

        assertNotNull(result);
        assertEquals(List.of("E-true"), result.get("q4"));
    }

    // Edge case: No options
    @Test
    void testGetQumlAnswersV2_NoOptions() throws Exception {
        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswersV2", List.class, Map.class);
        method.setAccessible(true);

        Map<String, Object> mcqSca = new HashMap<>();
        mcqSca.put(Constants.IDENTIFIER, "q5");
        mcqSca.put(Constants.QUESTION_TYPE, Constants.MCQ_SCA);
        Map<String, Object> editorStateSca = new HashMap<>();
        editorStateSca.put(Constants.OPTIONS, Collections.emptyList());
        mcqSca.put(Constants.EDITOR_STATE, editorStateSca);

        Map<String, Object> questionMap = Map.of("q5", mcqSca);
        List<String> questions = List.of("q5");

        when(utilService.mapper.convertValue(any(), any(TypeReference.class))).thenAnswer(invocation -> invocation.getArgument(0));
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, questions, questionMap);

        assertNotNull(result);
        assertEquals(Collections.emptyList(), result.get("q5"));
    }

    @Test
    void testGetQumlAnswers_MCQ_SCA() throws Exception {
        AssessmentUtilServiceV2Impl utilService = new AssessmentUtilServiceV2Impl();
        String qid = "q1";
        Map<String, Object> question = new HashMap<>();
        question.put(Constants.IDENTIFIER, qid);
        question.put(Constants.QUESTION_TYPE, Constants.MCQ_SCA);
        Map<String, Object> editorState = new HashMap<>();
        Map<String, Object> option = new HashMap<>();
        option.put(Constants.ANSWER, true);
        option.put(Constants.VALUE, Map.of(Constants.VALUE, "A"));
        editorState.put(Constants.OPTIONS, List.of(option));
        question.put(Constants.EDITOR_STATE, editorState);
        Map<String, Object> questionMap = Map.of(qid, question);

        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswers", List.class, Map.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, List.of(qid), questionMap);
        assertEquals(List.of("A"), result.get(qid));
    }

    @Test
    void testGetQumlAnswers_MCQ_MCA() throws Exception {
        AssessmentUtilServiceV2Impl utilService = new AssessmentUtilServiceV2Impl();
        String qid = "q2";
        Map<String, Object> question = new HashMap<>();
        question.put(Constants.IDENTIFIER, qid);
        question.put(Constants.QUESTION_TYPE, Constants.MCQ_MCA);
        Map<String, Object> editorState = new HashMap<>();
        Map<String, Object> option1 = new HashMap<>();
        option1.put(Constants.ANSWER, true);
        option1.put(Constants.VALUE, Map.of(Constants.VALUE, "B"));
        Map<String, Object> option2 = new HashMap<>();
        option2.put(Constants.ANSWER, true);
        option2.put(Constants.VALUE, Map.of(Constants.VALUE, "C"));
        editorState.put(Constants.OPTIONS, List.of(option1, option2));
        question.put(Constants.EDITOR_STATE, editorState);
        Map<String, Object> questionMap = Map.of(qid, question);

        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswers", List.class, Map.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, List.of(qid), questionMap);
        assertEquals(List.of("B", "C"), result.get(qid));
    }

    @Test
    void testGetQumlAnswers_FTB() throws Exception {
        AssessmentUtilServiceV2Impl utilService = new AssessmentUtilServiceV2Impl();
        String qid = "q3";
        Map<String, Object> question = new HashMap<>();
        question.put(Constants.IDENTIFIER, qid);
        question.put(Constants.QUESTION_TYPE, Constants.FTB);
        Map<String, Object> editorState = new HashMap<>();
        Map<String, Object> option = new HashMap<>();
        option.put(Constants.ANSWER, true);
        option.put(Constants.VALUE, Map.of(Constants.BODY, "D"));
        editorState.put(Constants.OPTIONS, List.of(option));
        question.put(Constants.EDITOR_STATE, editorState);
        Map<String, Object> questionMap = Map.of(qid, question);

        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswers", List.class, Map.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, List.of(qid), questionMap);
        assertEquals(List.of("D"), result.get(qid));
    }

    @Test
    void testGetQumlAnswers_MTF() throws Exception {
        AssessmentUtilServiceV2Impl utilService = new AssessmentUtilServiceV2Impl();
        String qid = "q4";
        Map<String, Object> question = new HashMap<>();
        question.put(Constants.IDENTIFIER, qid);
        question.put(Constants.QUESTION_TYPE, Constants.MTF);
        Map<String, Object> editorState = new HashMap<>();
        Map<String, Object> option = new HashMap<>();
        option.put(Constants.ANSWER, true);
        option.put(Constants.VALUE, Map.of(Constants.VALUE, "E"));
        editorState.put(Constants.OPTIONS, List.of(option));
        question.put(Constants.EDITOR_STATE, editorState);
        Map<String, Object> questionMap = Map.of(qid, question);

        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswers", List.class, Map.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, List.of(qid), questionMap);
        assertEquals(List.of("E-true"), result.get(qid));
    }

    @Test
    void testGetQumlAnswers_NoOptions() throws Exception {
        AssessmentUtilServiceV2Impl utilService = new AssessmentUtilServiceV2Impl();
        String qid = "q5";
        Map<String, Object> question = new HashMap<>();
        question.put(Constants.IDENTIFIER, qid);
        question.put(Constants.QUESTION_TYPE, Constants.MCQ_SCA);
        Map<String, Object> editorState = new HashMap<>();
        editorState.put(Constants.OPTIONS, Collections.emptyList());
        question.put(Constants.EDITOR_STATE, editorState);
        Map<String, Object> questionMap = Map.of(qid, question);

        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswers", List.class, Map.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, List.of(qid), questionMap);
        assertEquals(Collections.emptyList(), result.get(qid));
    }

    @Test
    void testGetQumlAnswers_EmptyQuestion() throws Exception {
        AssessmentUtilServiceV2Impl utilService = new AssessmentUtilServiceV2Impl();
        String qid = "q6";
        Map<String, Object> questionMap = Map.of(qid, Collections.emptyMap());

        Method method = AssessmentUtilServiceV2Impl.class.getDeclaredMethod("getQumlAnswers", List.class, Map.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(utilService, List.of(qid), questionMap);
        assertEquals(Collections.emptyList(), result.get(qid));
    }

    @Test
    void testValidateQumlAssessmentV3_EmptyInputs() {
        Map<String, Object> result = utilService.validateQumlAssessmentV3(null, null, null, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
