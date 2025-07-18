package com.igot.cb.assessment.model;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class QuestionSetTest {

    @Test
    void testGettersAndSetters() {
        QuestionSet qs = new QuestionSet();
        Integer timeLimit = 90;
        Boolean isAssessment = true;
        List<Questions> questions = new ArrayList<>();
        Questions q = new Questions();
        questions.add(q);

        qs.setTimeLimit(timeLimit);
        qs.setIsAssessment(isAssessment);
        qs.setQuestions(questions);

        assertEquals(timeLimit, qs.getTimeLimit());
        assertEquals(isAssessment, qs.getIsAssessment());
        assertEquals(questions, qs.getQuestions());
    }

    @Test
    void testDefaultValues() {
        QuestionSet qs = new QuestionSet();
        assertNull(qs.getTimeLimit());
        assertNull(qs.getIsAssessment());
        assertNull(qs.getQuestions());
    }
}