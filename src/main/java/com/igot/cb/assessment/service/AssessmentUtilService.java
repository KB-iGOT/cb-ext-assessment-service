package com.igot.cb.assessment.service;

import com.igot.cb.assessment.model.QuestionSet;

import java.util.List;
import java.util.Map;

public interface AssessmentUtilService {

	Map<String, Object> validateAssessment(List<Map<String, Object>> questions);

	Map<String, Object> validateAssessment(List<Map<String, Object>> questions, Map<String, Object> answers);

	Map<String, Object> getAnswerKeyForAssessmentAuthoringPreview(Map<String, Object> contentMeta);

	QuestionSet removeAssessmentAnsKey(Object assessmentContent);
}
