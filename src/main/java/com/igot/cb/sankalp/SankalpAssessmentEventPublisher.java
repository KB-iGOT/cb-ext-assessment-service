package com.igot.cb.sankalp;

import com.igot.cb.common.util.Constants;
import com.igot.cb.core.producer.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publishes a message to the Sankalp assessment topic when a learner submits a Baseline or
 * Endline assessment (the parent course's courseCategory is in the configured list).
 *
 * Contract (Kafka key: user_id):
 * {"user_id", "primary_category", "course_category", "assessment_id", "submitted_on"}
 * - submitted_on: epoch milliseconds of the submit.
 */
@Component
public class SankalpAssessmentEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SankalpAssessmentEventPublisher.class);

    private final Producer producer;

    @Value("${sankalp.assessment.event.enabled:true}")
    private boolean enabled;

    @Value("${sankalp.assessment.event.topic:dev.sankalp.assessment}")
    private String topic;

    @Value("${sankalp.assessment.course.categories:Baseline Assessment,Endline Assessment}")
    private String courseCategories;

    public SankalpAssessmentEventPublisher(Producer producer) {
        this.producer = producer;
    }

    /**
     * @param submitRequest   the submit request (userId, identifier)
     * @param primaryCategory the assessment's primary category
     * @param courseCategory  the parent course's category
     */
    public void publishIfEligible(Map<String, Object> submitRequest, String primaryCategory, String courseCategory) {
        try {
            if (!enabled || submitRequest == null || !matches(courseCategories, courseCategory)) {
                return;
            }
            String userId = asString(submitRequest.get(Constants.USER_ID));
            String assessmentId = asString(submitRequest.get(Constants.IDENTIFIER));
            if (StringUtils.isAnyBlank(userId, assessmentId)) {
                logger.info("[SANKALP][ASSESSMENT][skipped] Missing userId / assessmentId: userId={}, assessmentId={}",
                        userId, assessmentId);
                return;
            }

            Map<String, Object> event = new LinkedHashMap<>();
            event.put(Constants.SANKALP_EVENT_USER_ID, userId);
            event.put(Constants.SANKALP_EVENT_PRIMARY_CATEGORY, primaryCategory);
            event.put(Constants.SANKALP_EVENT_COURSE_CATEGORY, courseCategory);
            event.put(Constants.SANKALP_EVENT_ASSESSMENT_ID, assessmentId);
            event.put(Constants.SANKALP_EVENT_SUBMITTED_ON, System.currentTimeMillis());
            producer.pushWithKey(topic, event, userId);
            logger.info("[SANKALP][ASSESSMENT][published] userId={}, assessmentId={}, courseCategory={}, topic={}",
                    userId, assessmentId, courseCategory, topic);
        } catch (Exception e) {
            logger.error("[SANKALP][ASSESSMENT][failed] Could not publish Sankalp assessment event", e);
        }
    }

    private static boolean matches(String commaSeparated, String value) {
        if (StringUtils.isAnyBlank(commaSeparated, value)) {
            return false;
        }
        return Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .anyMatch(v -> v.equalsIgnoreCase(value.trim()));
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
