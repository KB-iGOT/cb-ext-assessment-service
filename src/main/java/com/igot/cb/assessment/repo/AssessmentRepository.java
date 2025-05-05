package com.igot.cb.assessment.repo;

import java.time.Instant;
import java.util.Map;

public interface AssessmentRepository {

    boolean addUserAssesmentDataToDB(String userId, String assessmentId, Instant startTime, Instant endTime,
                                     Map<String, Object> questionSet, String status);

    Boolean updateUserAssesmentDataToDB(String userId, String assessmentIdentifier,
                                        Map<String, Object> submitAssessmentRequest, Map<String, Object> submitAssessmentResponse, String status,
                                        Instant startTime, Map<String, Object> saveSubmitAssessmentRequest);


}
