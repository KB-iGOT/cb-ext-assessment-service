package com.igot.cb.common.service;

import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.util.CbExtAssessmentServerProperties;
import com.igot.cb.common.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ContentServiceImpl implements ContentService{

    private Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    @Autowired
    private OutboundRequestHandlerServiceImpl outboundRequestHandlerService;
    @Autowired
    CbExtAssessmentServerProperties serverConfig;

    @Override
    public String getContentType(String resourceId) {
        String parentContentType = "";
        Map<String, Object> response = getHierarchyResponseMap(resourceId);
        if (Constants.OK.equalsIgnoreCase((String) response.get(Constants.RESPONSE_CODE))) {
            Map<String, Object> resultMap = (Map<String, Object>) response.get(Constants.RESULT);
            if (!ObjectUtils.isEmpty(resultMap)) {
                Map<String, Object> contentMap = (Map<String, Object>) resultMap.get(Constants.CONTENT);
                if (!ObjectUtils.isEmpty(contentMap)) {
                    parentContentType = (String) contentMap.get(Constants.CONTENT_TYPE_KEY);
                }
            }
        }
        return parentContentType;
    }

    public String getParentIdentifier(String resourceId) {
        String parentId = "";
        Map<String, Object> response = getHierarchyResponseMap(resourceId);
        if (Constants.OK.equalsIgnoreCase((String) response.get(Constants.RESPONSE_CODE))) {
            Map<String, Object> resultMap = (Map<String, Object>) response.get(Constants.RESULT);
            if (!ObjectUtils.isEmpty(resultMap)) {
                Map<String, Object> contentMap = (Map<String, Object>) resultMap.get(Constants.CONTENT);
                if (!ObjectUtils.isEmpty(contentMap)) {
                    parentId = (String) contentMap.get(Constants.PARENT);
                }
            }
        }
        return parentId;
    }

    @Override
    public String updateContentProgress(String userAuthToken, Map<String, Object> reqBody, String userId, SBApiResponse outgoingResponse) {
        String response = "";
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            headers.put(Constants.X_AUTH_TOKEN, userAuthToken);
            headers.put(Constants.AUTHORIZATION, serverConfig.getSbApiKey());

            Map<String, Object> req = new HashMap<>();
            Map<String, Object> request = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();

            Map<String, Object> reqObj = new HashMap<>();
            reqObj.put(Constants.CONTENT_ID_KEY, reqBody.get(Constants.IDENTIFIER));
            reqObj.put(Constants.COURSE_ID, reqBody.get(Constants.COURSE_ID));
            reqObj.put(Constants.BATCH_ID, reqBody.get(Constants.BATCH_ID));
            reqObj.put(Constants.STATUS, 2);
            reqObj.put("lastAccessTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSZ").format(new Date()));
            reqObj.put(Constants.COMPLETION_PERCENTAGE, 100);

            contents.add(reqObj);

            req.put(Constants.USER_ID, userId);
            req.put("contents", contents);
            request.put(Constants.REQUEST,req);

            Map<String, Object> apiResponse = outboundRequestHandlerService.fetchResultUsingPatch(
                    serverConfig.getCourseServiceHost() + serverConfig.getProgressUpdateEndPoint(),
                    request, headers);

            if ("OK".equals(apiResponse.get("responseCode"))) {
                response = Constants.SUCCESS;
                logger.info(String.format("Successfully updated progress for user : %s, for assessment : %s, of course :%s", userId,
                        reqBody.get(Constants.IDENTIFIER),reqBody.get(Constants.COURSE_ID)));
            } else {
                logger.info(String.format("Failed to update progress for user : %s, for assessment : %s, of course :%s", userId,
                        reqBody.get(Constants.IDENTIFIER),reqBody.get(Constants.COURSE_ID)));
                outgoingResponse.setResult(null);
                updateErrorDetails(outgoingResponse, Constants.FAILED_TO_UPDATE_PROGRESS, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            logger.error(String.format("Failed to update progress for user: %s, for assessment: %s, of course: %s. Exception: %s",
                    userId, reqBody.get(Constants.IDENTIFIER),reqBody.get(Constants.COURSE_ID), e.getMessage()), e);
            outgoingResponse.setResult(null);
            updateErrorDetails(outgoingResponse, Constants.FAILED_TO_UPDATE_PROGRESS, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private void updateErrorDetails(SBApiResponse response, String errMsg, HttpStatus responseCode) {
        response.getParams().setStatus(Constants.FAILED);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(responseCode);
    }

    public Map<String, Object> getHierarchyResponseMap(String contentId) {
        StringBuilder url = new StringBuilder();
        url.append(serverConfig.getContentHost()).append(serverConfig.getHierarchyEndPoint()).append("/" + contentId)
                .append("?hierarchyType=detail");
        Map<String, Object> response = (Map<String, Object>) outboundRequestHandlerService.fetchResult(url.toString());
        if (ObjectUtils.isEmpty(response)) {
            return Collections.EMPTY_MAP;
        }

        return response;
    }
}
