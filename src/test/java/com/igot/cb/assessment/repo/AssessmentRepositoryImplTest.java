package com.igot.cb.assessment.repo;

import com.igot.cb.cassandra.utils.CassandraOperation;
import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssessmentRepositoryImplTest {

    @InjectMocks
    AssessmentRepositoryImpl repo;

    @Mock
    CassandraOperation cassandraOperation;
    @Mock
    UserAssessmentSummaryRepository userAssessmentSummaryRepo;
    @Mock
    UserAssessmentMasterRepository userAssessmentMasterRepo;
    @Mock
    UserQuizMasterRepository userQuizMasterRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- addUserAssesmentDataToDB ---

    @Test
    void testAddUserAssesmentDataToDB_Positive() {
        SBApiResponse resp = mock(SBApiResponse.class);
        Map<String, Object> result = new HashMap<>();
        result.put("STATUS", "SUCCESS");
        when(resp.getResult()).thenReturn(result);
        when(cassandraOperation.insertRecord(any(), any(), any())).thenReturn(resp);

        boolean res = repo.addUserAssesmentDataToDB("u1", "a1", Instant.now(), Instant.now(), Map.of("q", "v"), "COMPLETED");
        assertTrue(res);
    }

    @Test
    void testAddUserAssesmentDataToDB_Negative() {
        SBApiResponse resp = mock(SBApiResponse.class);
        Map<String, Object> result = new HashMap<>();
        result.put("STATUS", "FAILED");
        when(resp.getResult()).thenReturn(result);
        when(cassandraOperation.insertRecord(any(), any(), any())).thenReturn(resp);

        boolean res = repo.addUserAssesmentDataToDB("u1", "a1", Instant.now(), Instant.now(), Map.of(), "FAILED");
        assertFalse(res);
    }

    @Test
    void testAddUserAssesmentDataToDB_NullResponse() {
        when(cassandraOperation.insertRecord(any(), any(), any())).thenReturn(null);
        boolean res = repo.addUserAssesmentDataToDB("u1", "a1", Instant.now(), Instant.now(), Map.of(), "FAILED");
        assertFalse(res);
    }

    // --- updateUserAssesmentDataToDB ---

    @Test
    void testUpdateUserAssesmentDataToDB_Positive() {
        Map<String, Object> req = new HashMap<>();
        req.put(Constants.LANGUAGE, "EN");
        Map<String, Object> resp = Map.of("score", 1);
        Map<String, Object> saveReq = Map.of("save", 1);

        Boolean result = repo.updateUserAssesmentDataToDB("u1", "a1", req, resp, "COMPLETED", Instant.now(), saveReq);
        assertTrue(result);
        verify(cassandraOperation).updateRecord(any(), any(), any(), any());
    }

    @Test
    void testUpdateUserAssesmentDataToDB_NullOrEmptyInputs() {
        Boolean result = repo.updateUserAssesmentDataToDB("u1", "a1", new HashMap<>(), new HashMap<>(), "", Instant.now(), new HashMap<>());
        assertTrue(result);
        verify(cassandraOperation).updateRecord(any(), any(), any(), any());
    }

    // --- insertQuizOrAssessment ---

    @Test
    void testInsertQuizOrAssessment_Assessment_Positive() throws Exception {
        Map<String, Object> persist = new HashMap<>();
        persist.put("rootOrg", "org");
        persist.put("parent", "parent");
        persist.put("result", 10.0);
        persist.put("correct", 2);
        persist.put("incorrect", 1);
        persist.put("blank", 0);
        persist.put("parentContentType", "course");
        persist.put("sourceId", "src");
        persist.put("title", "title");
        persist.put("userId", "u1");

        UserAssessmentSummaryModel summary = mock(UserAssessmentSummaryModel.class);
        when(summary.getFirstMaxScore()).thenReturn(5f);
        when(summary.getFirstPassesScore()).thenReturn(5f);
        when(summary.getFirstPassesScoreDate()).thenReturn(new Date());
        when(userAssessmentSummaryRepo.findById(any())).thenReturn(Optional.of(summary));

        Map<String, Object> res = repo.insertQuizOrAssessment(persist, true);
        assertEquals("SUCCESS", res.get("response"));
    }

    @Test
    void testInsertQuizOrAssessment_Assessment_NullSummary() throws Exception {
        Map<String, Object> persist = new HashMap<>();
        persist.put("rootOrg", "org");
        persist.put("parent", "parent");
        persist.put("result", 10.0);
        persist.put("correct", 2);
        persist.put("incorrect", 1);
        persist.put("blank", 0);
        persist.put("parentContentType", "course");
        persist.put("sourceId", "src");
        persist.put("title", "title");
        persist.put("userId", "u1");

        when(userAssessmentSummaryRepo.findById(any())).thenReturn(Optional.empty());

        Map<String, Object> res = repo.insertQuizOrAssessment(persist, true);
        assertEquals("SUCCESS", res.get("response"));
    }

    @Test
    void testInsertQuizOrAssessment_Quiz_Positive() throws Exception {
        Map<String, Object> persist = new HashMap<>();
        persist.put("rootOrg", "org");
        persist.put("result", 10.0);
        persist.put("correct", 2);
        persist.put("incorrect", 1);
        persist.put("blank", 0);
        persist.put("sourceId", "src");
        persist.put("title", "title");
        persist.put("userId", "u1");

        Map<String, Object> res = repo.insertQuizOrAssessment(persist, false);
        assertEquals("SUCCESS", res.get("response"));
    }

    // --- getAssessmentbyContentUser ---

    @Test
    void testGetAssessmentbyContentUser_AlwaysEmpty() throws Exception {
        List<Map<String, Object>> res = repo.getAssessmentbyContentUser("org", "cid", "uid");
        assertTrue(res.isEmpty());
    }

    // --- fetchUserAssessmentDataFromDB ---

    @Test
    void testFetchUserAssessmentDataFromDB_Positive() {
        List<Map<String, Object>> data = List.of(Map.of("k", "v"));
        when(cassandraOperation.getRecordsByProperties(any(), any(), any(), any())).thenReturn(data);

        List<Map<String, Object>> res = repo.fetchUserAssessmentDataFromDB("u1", "a1");
        assertEquals(1, res.size());
    }

    @Test
    void testFetchUserAssessmentDataFromDB_Empty() {
        when(cassandraOperation.getRecordsByProperties(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        List<Map<String, Object>> res = repo.fetchUserAssessmentDataFromDB("u1", "a1");
        assertTrue(res.isEmpty());
    }
}