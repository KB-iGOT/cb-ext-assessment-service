package com.igot.cb.assessment.repo;

import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class UserAssessmentSummaryModelTest {

    @Test
    void testGettersAndSetters() {
        UserAssessmentSummaryPrimaryKeyModel pk = new UserAssessmentSummaryPrimaryKeyModel();
        Float maxScore = 10.5f;
        Date maxScoreDate = new Date();
        Float passScore = 8.0f;
        Date passScoreDate = new Date();

        UserAssessmentSummaryModel model = new UserAssessmentSummaryModel();
        model.setPrimaryKey(pk);
        model.setFirstMaxScore(maxScore);
        model.setFirstMaxScoreDate(maxScoreDate);
        model.setFirstPassesScore(passScore);
        model.setFirstPassesScoreDate(passScoreDate);

        assertEquals(pk, model.getPrimaryKey());
        assertEquals(maxScore, model.getFirstMaxScore());
        assertEquals(maxScoreDate, model.getFirstMaxScoreDate());
        assertEquals(passScore, model.getFirstPassesScore());
        assertEquals(passScoreDate, model.getFirstPassesScoreDate());
    }

    @Test
    void testAllArgsConstructorAndToString() {
        UserAssessmentSummaryPrimaryKeyModel pk = new UserAssessmentSummaryPrimaryKeyModel();
        Float maxScore = 9.0f;
        Date maxScoreDate = new Date();
        Float passScore = 7.0f;
        Date passScoreDate = new Date();

        UserAssessmentSummaryModel model = new UserAssessmentSummaryModel(
                pk, maxScore, maxScoreDate, passScore, passScoreDate
        );

        assertEquals(pk, model.getPrimaryKey());
        assertEquals(maxScore, model.getFirstMaxScore());
        assertEquals(maxScoreDate, model.getFirstMaxScoreDate());
        assertEquals(passScore, model.getFirstPassesScore());
        assertEquals(passScoreDate, model.getFirstPassesScoreDate());
        assertTrue(model.toString().contains("UserAssessmentSummaryModel"));
    }
}
