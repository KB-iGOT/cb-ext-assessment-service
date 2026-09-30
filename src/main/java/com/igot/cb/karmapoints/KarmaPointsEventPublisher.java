package com.igot.cb.karmapoints;

import com.igot.cb.common.util.Constants;
import com.igot.cb.core.producer.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Publishes the karma points assessment events to the unified topic.
 *
 * For a saved, passed final (course) assessment:
 * - ASSESSMENT_PASSED is published on every passed attempt (KPI 2.3);
 * - ASSESSMENT_HIGH_SCORE is published as well when the score is at or above the configured
 *   threshold (default 75, inclusive)
 * Contract (same shape as SELF_REGISTRATION / VERIFIED_PROFILE / SURVEY_SUBMISSION):
 * {"eventType": "ASSESSMENT_PASSED" | "ASSESSMENT_HIGH_SCORE",
 *  "data": {"edata": {"userId", "courseId", "batchId", "assessmentId", "score"}}, "version": 1}
 */
@Component
public class KarmaPointsEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(KarmaPointsEventPublisher.class);

    public static final String EVENT_TYPE_ASSESSMENT_PASSED = "ASSESSMENT_PASSED";
    public static final String EVENT_TYPE_ASSESSMENT_HIGH_SCORE = "ASSESSMENT_HIGH_SCORE";

    @Autowired
    private Producer producer;

    @Value("${karma.points.unified.event.topic:dev.karma.points.unified.v2.event}")
    private String karmaPointsUnifiedEventTopic;

    @Value("${karma.points.assessment.event.enabled:true}")
    private boolean assessmentEventEnabled;

    @Value("${karma.points.assessment.eligible.primary.categories:Course Assessment}")
    private String eligiblePrimaryCategories;

    @Value("${karma.points.assessment.excluded.course.categories:Program,Curated Program,Blended Program}")
    private String excludedCourseCategories;

    @Value("${karma.points.assessment.high.score.threshold:75}")
    private double highScoreThreshold;

    @Value("${karma.points.event.version:1}")
    private int eventVersion;

    /**
     * @param submitRequest   the submit request (userId, courseId, batchId, identifier)
     * @param result          the computed result (pass, overallResult)
     * @param primaryCategory the assessment's primary category
     * @param courseCategory  the parent course category, or null when the caller doesn't know it
     */
    public void publishAssessmentKarmaEventsIfEligible(Map<String, Object> submitRequest, Map<String, Object> result,
                                                       String primaryCategory, String courseCategory) {
        try {
            if (!assessmentEventEnabled || submitRequest == null || result == null) {
                return;
            }
            String userId = asString(submitRequest.get(Constants.USER_ID));
            String courseId = asString(submitRequest.get(Constants.COURSE_ID));
            String assessmentId = asString(submitRequest.get(Constants.IDENTIFIER));
            boolean passed = Boolean.TRUE.equals(result.get(Constants.PASS));
            Double score = asDouble(result.get(Constants.OVERALL_RESULT));
            String logCtx = "userId=" + userId + ", courseId=" + courseId + ", assessmentId=" + assessmentId
                    + ", primaryCategory=" + primaryCategory + ", courseCategory=" + courseCategory
                    + ", pass=" + passed + ", score=" + score;

            if (!passed) {
                logger.info("[KARMA_POINTS][ASSESSMENT][skipped] Not passed: {}", logCtx);
                return;
            }
            if (!matches(eligiblePrimaryCategories, primaryCategory)) {
                logger.info("[KARMA_POINTS][ASSESSMENT][skipped] Not a final course assessment: {}", logCtx);
                return;
            }
            if (StringUtils.isNotBlank(courseCategory) && matches(excludedCourseCategories, courseCategory)) {
                logger.info("[KARMA_POINTS][ASSESSMENT][skipped] Program / CAP is excluded: {}", logCtx);
                return;
            }
            if (StringUtils.isAnyBlank(userId, courseId, assessmentId)) {
                logger.info("[KARMA_POINTS][ASSESSMENT][skipped] Missing userId / courseId / assessmentId: {}", logCtx);
                return;
            }

            Map<String, Object> edata = new HashMap<>();
            edata.put("userId", userId);
            edata.put("courseId", courseId);
            edata.put("batchId", StringUtils.defaultString(asString(submitRequest.get(Constants.BATCH_ID))));
            edata.put("assessmentId", assessmentId);
            edata.put("score", score);

            // KPI 2.3: every passed attempt (the karma job awards it once per user and course)
            publish(EVENT_TYPE_ASSESSMENT_PASSED, edata, userId, logCtx);

            // KPI 2.5: passed with score >= threshold
            if (score != null && score >= highScoreThreshold) {
                publish(EVENT_TYPE_ASSESSMENT_HIGH_SCORE, edata, userId, logCtx);
            } else {
                logger.info("[KARMA_POINTS][ASSESSMENT_HIGH_SCORE][skipped] Score below {}: {}", highScoreThreshold, logCtx);
            }
        } catch (Exception e) {
            logger.error("[KARMA_POINTS][ASSESSMENT][failed] Could not publish karma event", e);
        }
    }

    private void publish(String eventType, Map<String, Object> edata, String userId, String logCtx) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("edata", new HashMap<>(edata));
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("data", data);
            event.put("version", eventVersion);
            producer.pushWithKey(karmaPointsUnifiedEventTopic, event, userId);
            logger.info("[KARMA_POINTS][{}][published] {}, topic={}", eventType, logCtx, karmaPointsUnifiedEventTopic);
        } catch (Exception e) {
            logger.error("[KARMA_POINTS][" + eventType + "][failed] Could not publish karma event: " + logCtx, e);
        }
    }

    private static boolean matches(String commaSeparated, String value) {
        if (StringUtils.isBlank(value) || StringUtils.isBlank(commaSeparated)) {
            return false;
        }
        List<String> values = Arrays.stream(commaSeparated.split(","))
                .map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        return values.stream().anyMatch(v -> v.equalsIgnoreCase(value.trim()));
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Double asDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
