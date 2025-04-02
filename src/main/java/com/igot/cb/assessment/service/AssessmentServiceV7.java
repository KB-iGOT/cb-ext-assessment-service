package com.igot.cb.assessment.service;

import com.igot.cb.common.model.SBApiResponse;

import java.util.Map;

public interface AssessmentServiceV7 {
    public SBApiResponse submitAssessmentAsyncV7(Map<String, Object> data, String userAuthToken, boolean editMode);

    public SBApiResponse retakeAssessmentV7(String assessmentIdentifier, String token, Boolean editMode);

    public SBApiResponse readAssessmentV7(String assessmentIdentifier, String token, boolean editMode);

    public SBApiResponse readAssessmentResultV7(Map<String, Object> request, String userAuthToken);

}
