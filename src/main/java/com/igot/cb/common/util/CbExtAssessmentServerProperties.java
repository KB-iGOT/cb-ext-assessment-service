package com.igot.cb.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CbExtAssessmentServerProperties {
    @Value("${user.assessment.submission.duration}")
    private String userAssessmentSubmissionDuration;


    @Value("${kafka.topics.user.assessment.submit}")
    private String assessmentSubmitTopic;

    @Value("${assessment.read.assessmentLevel.params}")
    private String assessmentLevelParams;

    @Value("${assessment.read.sectionLevel.params}")
    private String assessmentSectionParams;

    @Value("${assessment.host}")
    private String assessmentHost;

    @Value("${assessment.question.list.path}")
    private String assessmentQuestionListPath;

    @Value("${sb.api.key}")
    private String sbApiKey;

    @Value("${assessment.hierarchy.read.path}")
    private String assessmentHierarchyReadPath;

    @Value("${read.assess.questions.from.redis}")
    private boolean qListFromCacheEnabled;

    @Value("${assessment.hierarchy.table}")
    private String assessmentHierarchyTable;

    @Value("${assessment.hierarchy.namespace}")
    private String assessmentHierarchyNameSpace;

    @Value("${redis.questions.read.timeout}")
    private Integer redisQuestionsReadTimeOut;

    @Value("${assessment.user.submit.data.table}")
    private String assessmentUserSubmitDataTable;

    @Value("${redis.timeout}")
    private String redisTimeout;

    @Value("${sunbird.course.service.host}")
    private String courseServiceHost;
    @Value("${progress.api.update.endpoint}")
    private String progressUpdateEndPoint;

    @Value("${content-service-host}")
    private String contentHost;

    @Value("${content-hierarchy-endpoint}")
    private String hierarchyEndPoint;

    @Value("${sb.service.url}")
    private String sbUrl;

    @Value("${sunbird.user.search.endpoint}")
    private String userSearchEndPoint;

    @Value("${assessment.read.questionLevel.params}")
    private String assessmentQuestionParams;

    public List<String> getAssessmentQuestionParams() {
        return Arrays.asList(assessmentQuestionParams.split(",", -1));
    }

    public void setAssessmentQuestionParams(String assessmentQuestionParams) {
        this.assessmentQuestionParams = assessmentQuestionParams;
    }

    public void setUserAssessmentSubmissionDuration(String userAssessmentSubmissionDuration) {
        this.userAssessmentSubmissionDuration = userAssessmentSubmissionDuration;
    }


    public String getUserAssessmentSubmissionDuration() {
        return userAssessmentSubmissionDuration;
    }

    public String getAssessmentSubmitTopic() {
        return assessmentSubmitTopic;
    }

    public List<String> getAssessmentLevelParams() {
        return Arrays.asList(assessmentLevelParams.split(",", -1));
    }

    public List<String> getAssessmentSectionParams() {
        return Arrays.asList(assessmentSectionParams.split(",", -1));
    }

    public String getAssessmentHost() {
        return assessmentHost;
    }

    public String getAssessmentQuestionListPath() {
        return assessmentQuestionListPath;
    }

    public String getSbApiKey() {
        return sbApiKey;
    }

    public String getAssessmentHierarchyReadPath() {
        return assessmentHierarchyReadPath;
    }

    public boolean qListFromCacheEnabled() {
        return qListFromCacheEnabled;
    }

    public String getAssessmentHierarchyTable() {
        return assessmentHierarchyTable;
    }

    public String getAssessmentHierarchyNameSpace() {
        return assessmentHierarchyNameSpace;
    }

    public Integer getRedisQuestionsReadTimeOut() {
        return redisQuestionsReadTimeOut;
    }

    public String getAssessmentUserSubmitDataTable() {
        return assessmentUserSubmitDataTable;
    }

    public String getRedisTimeout() {
        return redisTimeout;
    }

    public String getCourseServiceHost() {
        return courseServiceHost;
    }

    public String getProgressUpdateEndPoint() {
        return progressUpdateEndPoint;
    }

    public String getContentHost() {
        return contentHost;
    }

    public String getHierarchyEndPoint() {
        return hierarchyEndPoint;
    }

    public String getSbUrl() {
        return sbUrl;
    }

    public String getUserSearchEndPoint() {
        return userSearchEndPoint;
    }


    public void setAssessmentSubmitTopic(String assessmentSubmitTopic) {
        this.assessmentSubmitTopic = assessmentSubmitTopic;
    }

    public void setAssessmentLevelParams(String assessmentLevelParams) {
        this.assessmentLevelParams = assessmentLevelParams;
    }

    public void setAssessmentSectionParams(String assessmentSectionParams) {
        this.assessmentSectionParams = assessmentSectionParams;
    }

    public void setAssessmentHost(String assessmentHost) {
        this.assessmentHost = assessmentHost;
    }

    public void setAssessmentQuestionListPath(String assessmentQuestionListPath) {
        this.assessmentQuestionListPath = assessmentQuestionListPath;
    }

    public void setSbApiKey(String sbApiKey) {
        this.sbApiKey = sbApiKey;
    }

    public void setAssessmentHierarchyReadPath(String assessmentHierarchyReadPath) {
        this.assessmentHierarchyReadPath = assessmentHierarchyReadPath;
    }

    public void setqListFromCacheEnabled(boolean qListFromCacheEnabled) {
        this.qListFromCacheEnabled = qListFromCacheEnabled;
    }

    public void setAssessmentHierarchyTable(String assessmentHierarchyTable) {
        this.assessmentHierarchyTable = assessmentHierarchyTable;
    }

    public void setAssessmentHierarchyNameSpace(String assessmentHierarchyNameSpace) {
        this.assessmentHierarchyNameSpace = assessmentHierarchyNameSpace;
    }

    public void setRedisQuestionsReadTimeOut(Integer redisQuestionsReadTimeOut) {
        this.redisQuestionsReadTimeOut = redisQuestionsReadTimeOut;
    }

    public void setAssessmentUserSubmitDataTable(String assessmentUserSubmitDataTable) {
        this.assessmentUserSubmitDataTable = assessmentUserSubmitDataTable;
    }

    public void setRedisTimeout(String redisTimeout) {
        this.redisTimeout = redisTimeout;
    }

    public void setCourseServiceHost(String courseServiceHost) {
        this.courseServiceHost = courseServiceHost;
    }

    public void setProgressUpdateEndPoint(String progressUpdateEndPoint) {
        this.progressUpdateEndPoint = progressUpdateEndPoint;
    }

    public void setContentHost(String contentHost) {
        this.contentHost = contentHost;
    }

    public void setHierarchyEndPoint(String hierarchyEndPoint) {
        this.hierarchyEndPoint = hierarchyEndPoint;
    }

    public void setSbUrl(String sbUrl) {
        this.sbUrl = sbUrl;
    }

    public void setUserSearchEndPoint(String userSearchEndPoint) {
        this.userSearchEndPoint = userSearchEndPoint;
    }
}
