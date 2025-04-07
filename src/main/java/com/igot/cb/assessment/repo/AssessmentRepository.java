package com.igot.cb.assessment.repo;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public interface AssessmentRepository {

    boolean addUserAssesmentDataToDB(String userId, String assessmentId, Timestamp startTime, Timestamp endTime,
                                     Map<String, Object> questionSet, String status);

    Boolean updateUserAssesmentDataToDB(String userId, String assessmentIdentifier,
                                        Map<String, Object> submitAssessmentRequest, Map<String, Object> submitAssessmentResponse, String status,
                                        Date startTime, Map<String, Object> saveSubmitAssessmentRequest);


}
