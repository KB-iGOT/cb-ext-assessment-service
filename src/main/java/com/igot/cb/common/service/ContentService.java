package com.igot.cb.common.service;

import java.util.List;
import java.util.Map;
import com.igot.cb.common.model.SBApiResponse;

public interface ContentService {
    Object getContentType(String parentId);

    String getParentIdentifier(String identifier);

    public String updateContentProgress(String userAuthToken, Map<String, Object> reqBody, String userId, SBApiResponse outgoingResponse);

    public Map<String, Object> readContentFromCache(String contentId, List<String> fields);

}
