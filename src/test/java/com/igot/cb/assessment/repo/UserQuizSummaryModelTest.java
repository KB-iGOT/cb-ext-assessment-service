package com.igot.cb.assessment.repo;


import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class UserQuizSummaryModelTest {

    @Test
    void testNoArgsConstructorAndSettersGetters() {
        UserQuizSummaryPrimaryKeyModel pk = new UserQuizSummaryPrimaryKeyModel();
        Date date = new Date();

        UserQuizSummaryModel model = new UserQuizSummaryModel();
        model.setPrimaryKey(pk);
        model.setDateUpdated(date);

        assertEquals(pk, model.getPrimaryKey());
        assertEquals(date, model.getDateUpdated());
    }

    @Test
    void testAllArgsConstructorAndToString() {
        UserQuizSummaryPrimaryKeyModel pk = new UserQuizSummaryPrimaryKeyModel();
        Date date = new Date();

        UserQuizSummaryModel model = new UserQuizSummaryModel(pk, date);

        assertEquals(pk, model.getPrimaryKey());
        assertEquals(date, model.getDateUpdated());
        assertTrue(model.toString().contains("UserQuizSummaryModel"));
    }
}