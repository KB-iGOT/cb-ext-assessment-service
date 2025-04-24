package com.igot.cb.assessment.repo;

import com.google.gson.Gson;
import com.igot.cb.cassandra.utils.CassandraOperation;
import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.util.Constants;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class AssessmentRepositoryImpl implements AssessmentRepository {

    @Autowired
    CassandraOperation cassandraOperation;

    @Override
    public boolean addUserAssesmentDataToDB(String userId, String assessmentIdentifier, Instant startTime,
                                            Instant endTime, Map<String, Object> questionSet, String status) {
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.USER_ID, userId);
        request.put(Constants.ASSESSMENT_ID_KEY, assessmentIdentifier);
        request.put(Constants.START_TIME, startTime);
        request.put(Constants.END_TIME, endTime);
        request.put(Constants.ASSESSMENT_READ_RESPONSE, new Gson().toJson(questionSet));
        request.put(Constants.STATUS, status);
        SBApiResponse resp = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD,
                Constants.TABLE_USER_ASSESSMENT_DATA, request);
        return resp.get(Constants.RESPONSE).equals(Constants.SUCCESS);
    }


    @Override
    public Boolean updateUserAssesmentDataToDB(String userId, String assessmentIdentifier,
                                               Map<String, Object> submitAssessmentRequest, Map<String, Object> submitAssessmentResponse, String status,
                                               Date startTime,Map<String, Object> saveSubmitAssessmentRequest) {
        Map<String, Object> compositeKeys = new HashMap<>();
        compositeKeys.put(Constants.USER_ID, userId);
        compositeKeys.put(Constants.ASSESSMENT_ID_KEY, assessmentIdentifier);
        compositeKeys.put(Constants.START_TIME, startTime);
        Map<String, Object> fieldsToBeUpdated = new HashMap<>();
        if (MapUtils.isNotEmpty(submitAssessmentRequest)) {
            fieldsToBeUpdated.put("submitassessmentrequest", new Gson().toJson(submitAssessmentRequest));
        }
        if (MapUtils.isNotEmpty(submitAssessmentResponse)) {
            fieldsToBeUpdated.put("submitassessmentresponse", new Gson().toJson(submitAssessmentResponse));
        }
        if (StringUtils.isNotBlank(status)) {
            fieldsToBeUpdated.put(Constants.STATUS, status);
        }
        if (MapUtils.isNotEmpty(saveSubmitAssessmentRequest)) {
            fieldsToBeUpdated.put("savepointsubmitreq", new Gson().toJson(saveSubmitAssessmentRequest));
        }
        cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_USER_ASSESSMENT_DATA,
                fieldsToBeUpdated, compositeKeys);
        return true;
    }
}