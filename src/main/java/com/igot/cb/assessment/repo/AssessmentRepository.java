package com.igot.cb.assessment.repo;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface AssessmentRepository {

    boolean addUserAssesmentDataToDB(String userId, String assessmentId, Instant startTime, Instant endTime,
                                     Map<String, Object> questionSet, String status);

    Boolean updateUserAssesmentDataToDB(String userId, String assessmentIdentifier,
                                        Map<String, Object> submitAssessmentRequest, Map<String, Object> submitAssessmentResponse, String status,
                                        Instant startTime, Map<String, Object> saveSubmitAssessmentRequest);


    /**
     * inserts quiz or assessments for a user
     *
     * @param persist
     * @param isAssessment
     * @return
     * @throws Exception
     */
    public Map<String, Object> insertQuizOrAssessment(Map<String, Object> persist, Boolean isAssessment)
            throws Exception;

    /**
     * gets assessment for a user given a content id
     *
     * @param courseId
     * @param userId
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> getAssessmentbyContentUser(String rootOrg, String courseId, String userId)
            throws Exception;

    List<Map<String, Object>> fetchUserAssessmentDataFromDB(String userId, String assessmentIdentifier);

    /**
     * Inserts a failed assessment audit record into the database for tracking and debugging purposes.
     *
     * @param userId        the ID of the user who attempted the assessment
     * @param assessmentId  the identifier of the assessment that failed
     * @param submitRequest the original submit request payload
     * @param errMessage    the error message describing the failure
     * @param methodName    the fully qualified method name where the failure occurred
     * @return true if the audit record was inserted successfully, false otherwise
     */
    boolean addFailedAssessmentAudit(String userId, String assessmentId, Map<String, Object> submitRequest,
                                     String errMessage, String methodName);

}
