package com.igot.cb.assessment.service;

import com.igot.cb.assessment.dto.AssessmentSubmissionDTO;

import java.util.Map;

public interface AssessmentService {

    public Map<String, Object> submitAssessment(String rootOrg, AssessmentSubmissionDTO data, String userEmail)
            throws Exception;

    Map<String, Object> getAssessmentByContentUser(String rootOrg, String courseId, String userId) throws Exception;

    Map<String, Object> submitAssessmentByIframe(String rootOrg, Map<String, Object> request) throws Exception;

    public Map<String, Object> getAssessmentContent(String courseId, String assessmentContentId);
}
