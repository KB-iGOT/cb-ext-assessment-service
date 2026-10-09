package com.igot.cb.karmapoints;

import com.igot.cb.common.util.Constants;
import com.igot.cb.core.producer.Producer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("KarmaPointsEventPublisher Tests")
class KarmaPointsEventPublisherTest {

    @Mock
    private Producer producer;

    private KarmaPointsEventPublisher publisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        publisher = new KarmaPointsEventPublisher(producer);
        // Set default values via reflection
        ReflectionTestUtils.setField(publisher, "assessmentEventEnabled", true);
        ReflectionTestUtils.setField(publisher, "karmaPointsUnifiedEventTopic", "dev.karma.points.unified.v2.event");
        ReflectionTestUtils.setField(publisher, "highScoreThreshold", 75.0);
        ReflectionTestUtils.setField(publisher, "eventVersion", 1);
        ReflectionTestUtils.setField(publisher, "eligiblePrimaryCategories", "Course Assessment");
        ReflectionTestUtils.setField(publisher, "excludedCourseCategories", "Program,Curated Program,Blended Program");
    }

    @Test
    @DisplayName("Should publish ASSESSMENT_PASSED event when assessment is passed")
    void testPublishAssessmentPassed() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(80.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer, times(2)).pushWithKey(anyString(), eventCaptor.capture(), eq("user123"));

        Map<String, Object> firstEvent = eventCaptor.getAllValues().get(0);
        assertEquals("ASSESSMENT_PASSED", firstEvent.get("eventType"));
        assertEquals(1, firstEvent.get("version"));
        verifyEventData(firstEvent, "user123", "course456", "batch789", "assessment001", 80.0);
    }

    @Test
    @DisplayName("Should publish both ASSESSMENT_PASSED and ASSESSMENT_HIGH_SCORE when score >= threshold")
    void testPublishBothEventsForHighScore() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer, times(2)).pushWithKey(anyString(), eventCaptor.capture(), eq("user123"));

        Map<String, Object> passedEvent = eventCaptor.getAllValues().get(0);
        Map<String, Object> highScoreEvent = eventCaptor.getAllValues().get(1);

        assertEquals("ASSESSMENT_PASSED", passedEvent.get("eventType"));
        assertEquals("ASSESSMENT_HIGH_SCORE", highScoreEvent.get("eventType"));
    }

    @Test
    @DisplayName("Should publish ASSESSMENT_HIGH_SCORE when score exactly equals threshold")
    void testPublishHighScoreAtThreshold() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(75.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer, times(2)).pushWithKey(anyString(), eventCaptor.capture(), eq("user123"));
    }

    @Test
    @DisplayName("Should publish only ASSESSMENT_PASSED when score is below threshold")
    void testSkipHighScoreBelowThreshold() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(70.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer, times(1)).pushWithKey(anyString(), eventCaptor.capture(), eq("user123"));

        Map<String, Object> event = eventCaptor.getValue();
        assertEquals("ASSESSMENT_PASSED", event.get("eventType"));
    }

    @Test
    @DisplayName("Should not publish when assessment is not passed")
    void testSkipWhenAssessmentNotPassed() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createFailedResult(45.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @ParameterizedTest(name = "[{index}] primaryCategory=\"{0}\", courseCategory=\"{1}\"")
    @MethodSource("ineligibleCategoryScenarios")
    @DisplayName("Should not publish when the category combination makes the assessment ineligible")
    void testSkipWhenCategoryMakesAssessmentIneligible(String primaryCategory, String courseCategory) {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, primaryCategory, courseCategory);

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @ParameterizedTest(name = "[{index}] primaryCategory=\"{0}\", courseCategory=\"{1}\"")
    @MethodSource("eligibleCategoryScenarios")
    @DisplayName("Should publish when the category combination is still eligible")
    void testPublishWhenCategoryStillEligible(String primaryCategory, String courseCategory) {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, primaryCategory, courseCategory);

        verify(producer, times(2)).pushWithKey(anyString(), any(), eq("user123"));
    }

    @Test
    @DisplayName("Should not publish when userId is missing")
    void testSkipWhenUserIdMissing() {
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.COURSE_ID, "course456");
        submitRequest.put(Constants.BATCH_ID, "batch789");
        submitRequest.put(Constants.IDENTIFIER, "assessment001");

        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should not publish when courseId is missing")
    void testSkipWhenCourseIdMissing() {
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.USER_ID, "user123");
        submitRequest.put(Constants.BATCH_ID, "batch789");
        submitRequest.put(Constants.IDENTIFIER, "assessment001");

        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should not publish when assessmentId is missing")
    void testSkipWhenAssessmentIdMissing() {
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.USER_ID, "user123");
        submitRequest.put(Constants.COURSE_ID, "course456");
        submitRequest.put(Constants.BATCH_ID, "batch789");

        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should use empty string for batchId when missing")
    void testBatchIdDefaultsToEmpty() {
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.USER_ID, "user123");
        submitRequest.put(Constants.COURSE_ID, "course456");
        submitRequest.put(Constants.IDENTIFIER, "assessment001");

        // Below the high-score threshold so exactly one event (ASSESSMENT_PASSED) is published
        Map<String, Object> result = createPassedResult(60.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer).pushWithKey(anyString(), eventCaptor.capture(), anyString());

        Map<String, Object> event = eventCaptor.getValue();
        Map<String, Object> data = (Map<String, Object>) event.get("data");
        Map<String, Object> edata = (Map<String, Object>) data.get("edata");
        assertEquals("", edata.get("batchId"));
    }

    @Test
    @DisplayName("Should not publish when submitRequest is null")
    void testSkipWhenSubmitRequestNull() {
        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(null, result, "Course Assessment", "Regular Course");

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should not publish when result is null")
    void testSkipWhenResultNull() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, null, "Course Assessment", "Regular Course");

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should not publish when assessmentEventEnabled is false")
    void testSkipWhenEventDisabled() {
        ReflectionTestUtils.setField(publisher, "assessmentEventEnabled", false);

        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should handle exception during publishing gracefully")
    void testHandleExceptionGracefully() {
        doThrow(new RuntimeException("Producer error")).when(producer).pushWithKey(anyString(), any(), anyString());

        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(85.0);

        assertDoesNotThrow(() -> publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course"));
        verify(producer, atLeastOnce()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should handle score as string")
    void testHandleScoreAsString() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.PASS, true);
        result.put(Constants.OVERALL_RESULT, "85.5");

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer, times(2)).pushWithKey(anyString(), eventCaptor.capture(), eq("user123"));

        Map<String, Object> event = eventCaptor.getAllValues().get(0);
        Map<String, Object> data = (Map<String, Object>) event.get("data");
        Map<String, Object> edata = (Map<String, Object>) data.get("edata");
        assertEquals(85.5, edata.get("score"));
    }

    @Test
    @DisplayName("Should handle invalid score string")
    void testHandleInvalidScoreString() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.PASS, true);
        result.put(Constants.OVERALL_RESULT, "invalid");

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer, times(1)).pushWithKey(anyString(), eventCaptor.capture(), eq("user123"));

        Map<String, Object> event = eventCaptor.getValue();
        Map<String, Object> data = (Map<String, Object>) event.get("data");
        Map<String, Object> edata = (Map<String, Object>) data.get("edata");
        assertNull(edata.get("score"));
    }

    @Test
    @DisplayName("Should handle score as Integer")
    void testHandleScoreAsInteger() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.PASS, true);
        result.put(Constants.OVERALL_RESULT, 90);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer, times(2)).pushWithKey(anyString(), eventCaptor.capture(), eq("user123"));

        Map<String, Object> event = eventCaptor.getAllValues().get(0);
        Map<String, Object> data = (Map<String, Object>) event.get("data");
        Map<String, Object> edata = (Map<String, Object>) data.get("edata");
        assertEquals(90.0, edata.get("score"));
    }

    @Test
    @DisplayName("Should handle score as null")
    void testHandleScoreAsNull() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.PASS, true);
        result.put(Constants.OVERALL_RESULT, null);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer, times(1)).pushWithKey(anyString(), eventCaptor.capture(), eq("user123"));

        Map<String, Object> event = eventCaptor.getValue();
        Map<String, Object> data = (Map<String, Object>) event.get("data");
        Map<String, Object> edata = (Map<String, Object>) data.get("edata");
        assertNull(edata.get("score"));
    }

    @Test
    @DisplayName("Should handle multiple eligible primary categories")
    void testMultipleEligibleCategories() {
        ReflectionTestUtils.setField(publisher, "eligiblePrimaryCategories", "Course Assessment, Final Assessment, Practice Assessment");

        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Final Assessment", "Regular Course");

        verify(producer, times(2)).pushWithKey(anyString(), any(), eq("user123"));
    }

    @Test
    @DisplayName("Should publish with correct event version")
    void testEventVersion() {
        ReflectionTestUtils.setField(publisher, "eventVersion", 2);

        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        // Below the high-score threshold so exactly one event (ASSESSMENT_PASSED) is published
        Map<String, Object> result = createPassedResult(60.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer).pushWithKey(anyString(), eventCaptor.capture(), anyString());

        Map<String, Object> event = eventCaptor.getValue();
        assertEquals(2, event.get("version"));
    }

    @Test
    @DisplayName("Should include all required edata fields in event")
    void testEventDataStructure() {
        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        // Below the high-score threshold so exactly one event (ASSESSMENT_PASSED) is published
        Map<String, Object> result = createPassedResult(60.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<Map<String, Object>> eventCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer).pushWithKey(anyString(), eventCaptor.capture(), eq("user123"));

        Map<String, Object> event = eventCaptor.getValue();
        assertTrue(event.containsKey("eventType"));
        assertTrue(event.containsKey("data"));
        assertTrue(event.containsKey("version"));

        Map<String, Object> data = (Map<String, Object>) event.get("data");
        assertTrue(data.containsKey("edata"));

        Map<String, Object> edata = (Map<String, Object>) data.get("edata");
        assertTrue(edata.containsKey("userId"));
        assertTrue(edata.containsKey("courseId"));
        assertTrue(edata.containsKey("batchId"));
        assertTrue(edata.containsKey("assessmentId"));
        assertTrue(edata.containsKey("score"));
    }

    @Test
    @DisplayName("Should use configured topic when publishing")
    void testPublishesToConfiguredTopic() {
        ReflectionTestUtils.setField(publisher, "karmaPointsUnifiedEventTopic", "custom.topic.name");

        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        // Below the high-score threshold so exactly one event (ASSESSMENT_PASSED) is published
        Map<String, Object> result = createPassedResult(60.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        verify(producer).pushWithKey(eq("custom.topic.name"), any(), eq("user123"));
    }

    @Test
    @DisplayName("Should use userId as partition key")
    void testUserIdAsPartitionKey() {
        Map<String, Object> submitRequest = createValidSubmitRequest("testUser999", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(producer, atLeastOnce()).pushWithKey(anyString(), any(), keyCaptor.capture());

        assertEquals("testUser999", keyCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle whitespace in comma-separated categories")
    void testHandleWhitespaceInCategories() {
        ReflectionTestUtils.setField(publisher, "excludedCourseCategories", " Program , Curated Program , Blended Program ");

        Map<String, Object> submitRequest = createValidSubmitRequest("user123", "course456", "batch789", "assessment001");
        Map<String, Object> result = createPassedResult(85.0);

        publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Program");

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should swallow an unexpected exception from a malformed field and not publish")
    void testHandleUnexpectedExceptionOutsidePublishGracefully() {
        Object poisonUserId = new Object() {
            @Override
            public String toString() {
                throw new IllegalStateException("boom");
            }
        };
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.USER_ID, poisonUserId);
        submitRequest.put(Constants.COURSE_ID, "course456");
        submitRequest.put(Constants.BATCH_ID, "batch789");
        submitRequest.put(Constants.IDENTIFIER, "assessment001");
        Map<String, Object> result = createPassedResult(85.0);

        assertDoesNotThrow(() ->
                publisher.publishAssessmentKarmaEventsIfEligible(submitRequest, result, "Course Assessment", "Regular Course"));

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    private static Stream<Arguments> ineligibleCategoryScenarios() {
        return Stream.of(
                Arguments.of("Self Assessment", "Regular Course"),   // primaryCategory not eligible
                Arguments.of("Course Assessment", "Program"),        // courseCategory excluded
                Arguments.of("Course Assessment", "Curated Program"),// courseCategory excluded
                Arguments.of("Course Assessment", "Blended Program"),// courseCategory excluded
                Arguments.of("   ", "Regular Course"),                // primaryCategory blank
                Arguments.of(null, "Regular Course")                  // primaryCategory null
        );
    }

    private static Stream<Arguments> eligibleCategoryScenarios() {
        return Stream.of(
                Arguments.of("Course Assessment", null),              // courseCategory null -> excluded check skipped
                Arguments.of("Course Assessment", "   "),              // courseCategory blank -> excluded check skipped
                Arguments.of("course assessment", "Regular Course")    // primaryCategory match is case-insensitive
        );
    }

    // Helper methods
    private Map<String, Object> createValidSubmitRequest(String userId, String courseId, String batchId, String assessmentId) {
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put(Constants.USER_ID, userId);
        submitRequest.put(Constants.COURSE_ID, courseId);
        submitRequest.put(Constants.BATCH_ID, batchId);
        submitRequest.put(Constants.IDENTIFIER, assessmentId);
        return submitRequest;
    }

    private Map<String, Object> createPassedResult(double score) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.PASS, true);
        result.put(Constants.OVERALL_RESULT, score);
        return result;
    }

    private Map<String, Object> createFailedResult(double score) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.PASS, false);
        result.put(Constants.OVERALL_RESULT, score);
        return result;
    }

    private void verifyEventData(Map<String, Object> event, String userId, String courseId, String batchId, String assessmentId, double score) {
        Map<String, Object> data = (Map<String, Object>) event.get("data");
        assertNotNull(data);
        Map<String, Object> edata = (Map<String, Object>) data.get("edata");
        assertNotNull(edata);
        assertEquals(userId, edata.get("userId"));
        assertEquals(courseId, edata.get("courseId"));
        assertEquals(batchId, edata.get("batchId"));
        assertEquals(assessmentId, edata.get("assessmentId"));
        assertEquals(score, edata.get("score"));
    }
}
