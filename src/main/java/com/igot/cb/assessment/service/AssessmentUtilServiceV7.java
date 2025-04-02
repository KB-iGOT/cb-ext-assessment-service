package com.igot.cb.assessment.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AssessmentUtilServiceV7 {


    List<Map<String, Object>> readQuestionDetails(List<String> identifiers);

    public Map<String, Object> getReadHierarchyApiResponse(String assessmentIdentifier, String token);

    public Map<String, Object> readAssessmentHierarchyFromCache(String assessmentIdentifier,boolean editMode,String token);

    public List<Map<String, Object>> readUserSubmittedAssessmentRecords(String userId, String assessmentId);

    public Map<String, Object> readQListfromCache(List<String> questionIds, String assessmentIdentifier,boolean editMode,String token) throws IOException;

    public Map<String,Object> fetchHierarchyFromAssessServc(String qSetId,String token);
/**
     * Validates a Quml assessment by comparing the original list of questions with the user's provided list of questions.
     *
     * @param questionSetDetailsMap a map containing details about the question set.
     * @param originalQuestionList  a list of original question identifiers.
     * @param userQuestionList      a list of maps where each map represents a user's question with its details.
     * @param questionMap           a map containing additional question-related information.
     * @return a map with validation results and resultMap.
     */
    Map<String, Object> validateQumlAssessmentV7(Map<String, Object> questionSetDetailsMap, List<String> originalQuestionList,
                                                 List<Map<String, Object>> userQuestionList, Map<String,Object> questionMap);
}
