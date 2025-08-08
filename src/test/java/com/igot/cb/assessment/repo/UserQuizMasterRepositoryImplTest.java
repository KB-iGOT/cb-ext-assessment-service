package com.igot.cb.assessment.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserQuizMasterRepositoryImplTest {

    @InjectMocks
    UserQuizMasterRepositoryImpl repo;

    @Mock
    CassandraOperations cassandraOperations;

    @Mock
    CassandraBatchOperations batchOps;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(cassandraOperations.batchOps()).thenReturn(batchOps);
    }

    @Test
    void testUpdateQuiz() {
        UserQuizMasterModel quiz = mock(UserQuizMasterModel.class);
        UserQuizSummaryModel quizSummary = mock(UserQuizSummaryModel.class);

        when(batchOps.insert(quiz)).thenReturn(batchOps);
        when(batchOps.insert(quizSummary)).thenReturn(batchOps);

        UserQuizMasterModel result = repo.updateQuiz(quiz, quizSummary);

        verify(batchOps).insert(quiz);
        verify(batchOps).insert(quizSummary);
        verify(batchOps).execute();
        assertEquals(quiz, result);
    }
}
