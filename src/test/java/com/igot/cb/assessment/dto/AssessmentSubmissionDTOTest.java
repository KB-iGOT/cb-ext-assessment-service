package com.igot.cb.assessment.dto;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentSubmissionDTOTest {

    @Test
    void testGettersAndSetters() {
        AssessmentSubmissionDTO dto = new AssessmentSubmissionDTO();
        Long timeLimit = 120L;
        Boolean isAssessment = true;
        List<Map<String, Object>> questions = new ArrayList<>();
        Map<String, Object> q1 = new HashMap<>();
        q1.put("id", "q1");
        questions.add(q1);
        String identifier = "assess-1";
        String title = "Assessment Title";

        dto.setTimeLimit(timeLimit);
        dto.setIsAssessment(isAssessment);
        dto.setQuestions(questions);
        dto.setIdentifier(identifier);
        dto.setTitle(title);

        assertEquals(timeLimit, dto.getTimeLimit());
        assertEquals(isAssessment, dto.isAssessment());
        assertEquals(questions, dto.getQuestions());
        assertEquals(identifier, dto.getIdentifier());
        assertEquals(title, dto.getTitle());
    }

    @Test
    void testToString() {
        AssessmentSubmissionDTO dto = new AssessmentSubmissionDTO();
        dto.setTimeLimit(60L);
        dto.setIsAssessment(false);
        dto.setQuestions(Collections.emptyList());
        dto.setIdentifier("id-123");
        dto.setTitle("Test Title");

        String str = dto.toString();
        assertTrue(str.contains("AssessmentSubmissionDTO"));
        assertTrue(str.contains("timeLimit=60"));
        assertTrue(str.contains("isAssessment=false"));
        assertTrue(str.contains("identifier=id-123"));
        assertTrue(str.contains("title=Test Title"));
    }
}
