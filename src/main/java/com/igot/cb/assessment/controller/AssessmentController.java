package com.igot.cb.assessment.controller;

import com.igot.cb.assessment.service.AssessmentServiceV7;
import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.util.Constants;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
public class AssessmentController {

    @Autowired
    AssessmentServiceV7 assessmentService;

    @PostMapping("/v7/user/assessment/submit")
    public ResponseEntity<SBApiResponse> submitUserAssessmentV7(@Valid @RequestBody Map<String, Object> requestBody,
                                                                @RequestHeader(Constants.X_AUTH_TOKEN) String authUserToken,
                                                                @RequestParam(name = "editMode" ,required = false) String editMode) {
        boolean edit = !StringUtils.isEmpty(editMode) && Boolean.parseBoolean(editMode);
        SBApiResponse submitResponse = assessmentService.submitAssessmentAsyncV7(requestBody, authUserToken,edit);
        return new ResponseEntity<>(submitResponse, submitResponse.getResponseCode());
    }

    @GetMapping("/v7/quml/assessment/retake/{assessmentIdentifier}")
    public ResponseEntity<SBApiResponse> retakeAssessmentV7(
            @PathVariable("assessmentIdentifier") String assessmentIdentifier,
            @RequestHeader(Constants.X_AUTH_TOKEN) String token,@RequestParam(name = "editMode" ,required = false) String editMode) {
        Boolean edit = !StringUtils.isEmpty(editMode) && Boolean.parseBoolean(editMode);
        SBApiResponse readResponse = assessmentService.retakeAssessmentV7(assessmentIdentifier, token,edit);
        return new ResponseEntity<>(readResponse, readResponse.getResponseCode());
    }

    @PostMapping("/v7/quml/assessment/read/{assessmentIdentifier}")
    public ResponseEntity<SBApiResponse> readAssessmentV7(
            @PathVariable("assessmentIdentifier") String assessmentIdentifier,
            @Valid @RequestBody Map<String, Object> requestBody,
            @RequestHeader(Constants.X_AUTH_TOKEN) String token,@RequestParam(name = "editMode" ,required = false) String editMode) {
        boolean edit = !StringUtils.isEmpty(editMode) && Boolean.parseBoolean(editMode);
        SBApiResponse readResponse = assessmentService.readAssessmentV7(assessmentIdentifier, requestBody, token, edit);
        return new ResponseEntity<>(readResponse, readResponse.getResponseCode());
    }

    @PostMapping("/v7/quml/assessment/result")
    public ResponseEntity<SBApiResponse> readAssessmentResultV7(@Valid @RequestBody Map<String, Object> requestBody,
                                                                @RequestHeader(Constants.X_AUTH_TOKEN) String authUserToken) {
        SBApiResponse response = assessmentService.readAssessmentResultV7(requestBody, authUserToken);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/v7/quml/question/list")
    public ResponseEntity<SBApiResponse> readQuestionListV7(@Valid @RequestBody Map<String, Object> requestBody, @RequestHeader("x-authenticated-user-token") String authUserToken, @RequestParam(name = "editMode", required = false) String editMode) {
        boolean edit = !StringUtils.isEmpty(editMode) && Boolean.parseBoolean(editMode);
        SBApiResponse response = assessmentService.readQuestionListV7(requestBody, authUserToken, edit);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
