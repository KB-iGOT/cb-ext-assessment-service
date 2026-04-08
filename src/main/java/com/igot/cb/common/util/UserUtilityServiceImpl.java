package com.igot.cb.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.common.model.SunbirdApiResp;
import com.igot.cb.core.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
@Service
public class UserUtilityServiceImpl implements UserUtilityService {


    private final Logger logger = LoggerFactory.getLogger(UserUtilityServiceImpl.class);

    @Autowired
    CbExtAssessmentServerProperties props;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public boolean validateUser(String rootOrg, String userId) {
        logger.info("Submit Assessment: rootOrg: " + rootOrg + ", userId: " + userId ); // remove after v2 api 400 error test
        Map<String, Object> requestMap = new HashMap<>();
        Map<String, Object> request = new HashMap<>();
        Map<String, String> filters = new HashMap<>();
        filters.put(Constants.USER_ID, userId);
        request.put(Constants.FILTERS, filters);

        requestMap.put(Constants.REQUEST, request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String reqBodyData = new ObjectMapper().writeValueAsString(requestMap);

            HttpEntity<String> requestEnty = new HttpEntity<>(reqBodyData, headers);

            String serverUrl = props.getSbUrl() + props.getUserSearchEndPoint();
            logger.info("requestMap" + requestMap ); // remove after v2 api 400 error test
            SunbirdApiResp sunbirdApiResp = restTemplate.postForObject(serverUrl, requestEnty, SunbirdApiResp.class);

            boolean expression = (sunbirdApiResp != null && "OK".equalsIgnoreCase(sunbirdApiResp.getResponseCode()) && sunbirdApiResp.getResult().getResponse().getCount() >= 1);
            return expression;

        } catch (Exception e) {
            throw new CustomException(Constants.ERROR, "Sunbird Service ERROR: ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
