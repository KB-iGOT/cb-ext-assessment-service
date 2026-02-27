package com.igot.cb.common.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.core.exception.ApplicationLogicError;

public interface ContentService {
    Object getContentType(String parentId);

    String getParentIdentifier(String identifier);

    public String updateContentProgress(String userAuthToken, Map<String, Object> reqBody, String userId, SBApiResponse outgoingResponse);

    public Map<String, Object> readContentFromCache(String contentId, List<String> fields);

    public Set<String> readChildCoursesFromCache(String parentDoId);

    public Map<String, Object> readContent(String contentId) throws ApplicationLogicError;

    String updatePreEnrolledAssessment(String userAuthToken, Map<String, Object> submitRequest, String userId, SBApiResponse contentUpdateResponse);
}
