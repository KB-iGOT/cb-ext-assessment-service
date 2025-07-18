package com.igot.cb.assessment.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraOperations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserAssessmentMasterRepositoryImplTest {

    @InjectMocks
    UserAssessmentMasterRepositoryImpl repo;

    @Mock
    CassandraOperations cassandraOperations;

    @Mock
    CassandraBatchOperations batchOps;

    @Mock
    UserAssessmentMasterModel assessment;

    @Mock
    UserAssessmentSummaryModel assessmentSummary;

    @Mock
    UserAssessmentSummaryPrimaryKeyModel summaryPrimaryKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(cassandraOperations.batchOps()).thenReturn(batchOps);
        when(batchOps.insert(any(UserAssessmentMasterModel.class))).thenReturn(batchOps);
        when(batchOps.insert(any(UserAssessmentSummaryModel.class))).thenReturn(batchOps);
        when(batchOps.execute()).thenReturn(null);
    }

    @Test
    void testUpdateAssessment_WithSummaryPrimaryKey() {
        when(assessmentSummary.getPrimaryKey()).thenReturn(summaryPrimaryKey);

        UserAssessmentMasterModel result = repo.updateAssessment(assessment, assessmentSummary);

        verify(batchOps).insert(assessment);
        verify(batchOps).insert(assessmentSummary);
        verify(batchOps).execute();
        assertEquals(assessment, result);
    }

    @Test
    void testUpdateAssessment_WithoutSummaryPrimaryKey() {
        when(assessmentSummary.getPrimaryKey()).thenReturn(null);

        UserAssessmentMasterModel result = repo.updateAssessment(assessment, assessmentSummary);

        verify(batchOps).insert(assessment);
        verify(batchOps, never()).insert(assessmentSummary);
        verify(batchOps).execute();
        assertEquals(assessment, result);
    }
}
