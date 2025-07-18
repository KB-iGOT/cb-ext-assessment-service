package com.igot.cb.assessment.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QuestionsTest {

    @Test
    void testGettersAndSetters() {
        Questions q = new Questions();
        String questionId = "q1";
        String question = "What is Java?";
        String questionType = "MCQ";
        List<Map<String, Object>> options = new ArrayList<>();
        Map<String, Object> option = new HashMap<>();
        option.put("optionId", "opt1");
        option.put("text", "A programming language");
        options.add(option);
        Boolean multiSelection = true;

        q.setQuestionId(questionId);
        q.setQuestion(question);
        q.setQuestionType(questionType);
        q.setOptions(options);
        q.setMultiSelection(multiSelection);

        assertEquals(questionId, q.getQuestionId());
        assertEquals(question, q.getQuestion());
        assertEquals(questionType, q.getQuestionType());
        assertEquals(options, q.getOptions());
        assertEquals(multiSelection, q.getMultiSelection());
    }

    @Test
    void testDefaultValues() {
        Questions q = new Questions();
        assertNull(q.getQuestionId());
        assertNull(q.getQuestion());
        assertNull(q.getQuestionType());
        assertNull(q.getOptions());
        assertNull(q.getMultiSelection());
    }
}
