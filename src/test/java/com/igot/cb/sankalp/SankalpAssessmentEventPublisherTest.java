package com.igot.cb.sankalp;

import com.igot.cb.common.util.Constants;
import com.igot.cb.core.producer.Producer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SankalpAssessmentEventPublisherTest {

    private static final String TOPIC = "dev.sankalp.assessment";
    private static final String USER_ID = "user-1";
    private static final String ASSESSMENT_ID = "do_qs_1";
    private static final String COURSE_ASSESSMENT = "Course Assessment";
    private static final String BASELINE = "Baseline Assessment";
    private static final String ENDLINE = "Endline Assessment";

    @Mock
    private Producer producer;

    private SankalpAssessmentEventPublisher publisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        publisher = new SankalpAssessmentEventPublisher(producer);
        ReflectionTestUtils.setField(publisher, "enabled", true);
        ReflectionTestUtils.setField(publisher, "topic", TOPIC);
        ReflectionTestUtils.setField(publisher, "courseCategories", BASELINE + "," + ENDLINE);
    }

    private Map<String, Object> submitRequest() {
        Map<String, Object> req = new HashMap<>();
        req.put(Constants.USER_ID, USER_ID);
        req.put(Constants.IDENTIFIER, ASSESSMENT_ID);
        return req;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> captureEvent() {
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(producer).pushWithKey(eq(TOPIC), captor.capture(), eq(USER_ID));
        return captor.getValue();
    }

    @Test
    void publishesBaselineSubmit() {
        publisher.publishIfEligible(submitRequest(), COURSE_ASSESSMENT, BASELINE);

        Map<String, Object> event = captureEvent();
        assertEquals(5, event.size());
        assertEquals(USER_ID, event.get(Constants.SANKALP_EVENT_USER_ID));
        assertEquals(COURSE_ASSESSMENT, event.get(Constants.SANKALP_EVENT_PRIMARY_CATEGORY));
        assertEquals(BASELINE, event.get(Constants.SANKALP_EVENT_COURSE_CATEGORY));
        assertEquals(ASSESSMENT_ID, event.get(Constants.SANKALP_EVENT_ASSESSMENT_ID));
        long submittedOn = (Long) event.get(Constants.SANKALP_EVENT_SUBMITTED_ON);
        assertTrue(Math.abs(System.currentTimeMillis() - submittedOn) < 60_000);
        assertFalse(event.containsKey("score"));
    }

    @Test
    void matchesCourseCategoryIgnoringCase() {
        publisher.publishIfEligible(submitRequest(), COURSE_ASSESSMENT, "endline assessment");

        assertEquals("endline assessment", captureEvent().get(Constants.SANKALP_EVENT_COURSE_CATEGORY));
    }

    @Test
    void skipsOtherCourseCategories() {
        publisher.publishIfEligible(submitRequest(), COURSE_ASSESSMENT, "Standalone Assessment");
        publisher.publishIfEligible(submitRequest(), COURSE_ASSESSMENT, null);

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    void skipsWhenDisabled() {
        ReflectionTestUtils.setField(publisher, "enabled", false);

        publisher.publishIfEligible(submitRequest(), COURSE_ASSESSMENT, BASELINE);

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    void skipsWhenUserIdMissing() {
        Map<String, Object> req = submitRequest();
        req.remove(Constants.USER_ID);

        publisher.publishIfEligible(req, COURSE_ASSESSMENT, BASELINE);

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    void skipsWhenRequestIsNull() {
        publisher.publishIfEligible(null, COURSE_ASSESSMENT, BASELINE);

        verify(producer, never()).pushWithKey(anyString(), any(), anyString());
    }

    @Test
    void producerFailureIsSwallowed() {
        doThrow(new RuntimeException("kafka down")).when(producer).pushWithKey(anyString(), any(), anyString());

        assertDoesNotThrow(() -> publisher.publishIfEligible(submitRequest(), COURSE_ASSESSMENT, BASELINE));
    }
}
