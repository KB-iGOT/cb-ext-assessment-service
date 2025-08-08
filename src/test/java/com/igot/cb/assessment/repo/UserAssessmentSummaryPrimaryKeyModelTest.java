package com.igot.cb.assessment.repo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserAssessmentSummaryPrimaryKeyModelTest {

    @Test
    void testNoArgsConstructorAndSettersGetters() {
        UserAssessmentSummaryPrimaryKeyModel model = new UserAssessmentSummaryPrimaryKeyModel();
        model.setRootOrg("org1");
        model.setUserId("user1");
        model.setContentId("content1");

        assertEquals("org1", model.getRootOrg());
        assertEquals("user1", model.getUserId());
        assertEquals("content1", model.getContentId());
    }

    @Test
    void testAllArgsConstructor() {
        UserAssessmentSummaryPrimaryKeyModel model =
                new UserAssessmentSummaryPrimaryKeyModel("org2", "user2", "content2");

        assertEquals("org2", model.getRootOrg());
        assertEquals("user2", model.getUserId());
        assertEquals("content2", model.getContentId());
    }

    @Test
    void testToString() {
        UserAssessmentSummaryPrimaryKeyModel model =
                new UserAssessmentSummaryPrimaryKeyModel("org3", "user3", "content3");
        String str = model.toString();
        assertTrue(str.contains("org3"));
        assertTrue(str.contains("user3"));
        assertTrue(str.contains("content3"));
    }
}
