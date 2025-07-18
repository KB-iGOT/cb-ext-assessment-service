package com.igot.cb.common.model;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SunbirdApiRespResultTest {

    @Test
    void testGettersAndSetters() {
        SunbirdApiRespResult result = new SunbirdApiRespResult();

        SunbirdApiResultResponse response = new SunbirdApiResultResponse();
        SunbirdApiHierarchyResultContent content = new SunbirdApiHierarchyResultContent();
        SunbirdApiHierarchyResultBatch batch = new SunbirdApiHierarchyResultBatch();
        Map<String, Object> questionSet = new HashMap<>();
        questionSet.put("key", "value");
        List<Map<String, Object>> questions = new ArrayList<>();
        Map<String, Object> question = new HashMap<>();
        question.put("q", "What is Java?");
        questions.add(question);

        result.setResponse(response);
        result.setContent(content);
        result.setBatch(batch);
        result.setQuestionSet(questionSet);
        result.setQuestions(questions);

        assertEquals(response, result.getResponse());
        assertEquals(content, result.getContent());
        assertEquals(batch, result.getBatch());
        assertEquals(questionSet, result.getQuestionSet());
        assertEquals(questions, result.getQuestions());
    }

    @Test
    void testDefaultValues() {
        SunbirdApiRespResult result = new SunbirdApiRespResult();

        assertNull(result.getResponse());
        assertNull(result.getContent());
        assertNull(result.getBatch());
        assertNull(result.getQuestionSet());
        assertNull(result.getQuestions());
    }
}
