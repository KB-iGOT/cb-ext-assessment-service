package com.igot.cb.cassandra.utils;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.igot.cb.common.helper.cassandra.CassandraConnectionManager;
import com.igot.cb.common.model.SBApiResponse;
import com.igot.cb.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CassandraOperationImplTest {

    @InjectMocks
    private CassandraOperationImpl cassandraOperation;

    @Mock
    private CassandraConnectionManager connectionManager;

    @Mock
    private CqlSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRecordsByProperties() {
        String keyspace = "ks";
        String table = "tbl";
        Map<String, Object> propertyMap = Map.of("id", "1");
        List<String> fields = List.of("id", "name");
        ResultSet resultSet = mock(ResultSet.class);

        when(connectionManager.getSession(keyspace)).thenReturn(session);
        when(session.execute(any(Statement.class))).thenReturn(resultSet);
        try (MockedStatic<CassandraUtil> util = mockStatic(CassandraUtil.class)) {
            util.when(() -> CassandraUtil.createResponse(resultSet)).thenReturn(List.of(Map.of("id", "1", "name", "test")));
            List<Map<String, Object>> result = cassandraOperation.getRecordsByProperties(keyspace, table, propertyMap, fields);
            assertEquals(1, result.size());
            assertEquals("1", result.get(0).get("id"));
        }
    }

    @Test
    void testGetCountByProperties() {
        String keyspace = "ks";
        String table = "tbl";
        Map<String, Object> propertyMap = Map.of("id", "1");
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        BoundStatement boundStatement = mock(BoundStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        Row row = mock(Row.class);

        when(connectionManager.getSession(keyspace)).thenReturn(session);
        when(session.prepare((SimpleStatement) any(Statement.class))).thenReturn(preparedStatement);
        when(preparedStatement.bind(any(Object[].class))).thenReturn(boundStatement);
        when(session.execute(boundStatement)).thenReturn(resultSet);
        when(resultSet.one()).thenReturn(row);
        when(row.getLong(0)).thenReturn(5L);

        int count = cassandraOperation.getCountByProperties(keyspace, table, propertyMap);
        assertEquals(5, count);
    }

    @Test
    void testInsertRecordSuccess() {
        String keyspace = "ks";
        String table = "tbl";
        Map<String, Object> request = Map.of("id", "1", "name", "test");
        PreparedStatement statement = mock(PreparedStatement.class);
        BoundStatement boundStatement = mock(BoundStatement.class);

        when(connectionManager.getSession(keyspace)).thenReturn(session);
        when(session.prepare(anyString())).thenReturn(statement);
        when(statement.bind(any(Object[].class))).thenReturn(boundStatement);

        try (MockedStatic<CassandraUtil> util = mockStatic(CassandraUtil.class)) {
            util.when(() -> CassandraUtil.getPreparedStatement(keyspace, table, request)).thenReturn("insert into ...");
            SBApiResponse response = cassandraOperation.insertRecord(keyspace, table, request);
            assertEquals("SUCCESS", response.get("STATUS"));
        }
    }

    @Test
    void testInsertRecordFailure() {
        String keyspace = "ks";
        String table = "tbl";
        Map<String, Object> request = Map.of("id", "1");
        when(connectionManager.getSession(keyspace)).thenThrow(new RuntimeException("fail"));

        SBApiResponse response = cassandraOperation.insertRecord(keyspace, table, request);
        assertEquals("FAILED", response.get("STATUS"));
    }

    @Test
    void testGetRecordsByPropertiesWithoutFiltering() {
        String keyspace = "ks";
        String table = "tbl";
        Map<String, Object> propertyMap = Map.of("id", "1");
        List<String> fields = List.of("id");
        ResultSet resultSet = mock(ResultSet.class);

        when(connectionManager.getSession(keyspace)).thenReturn(session);
        when(session.execute(any(SimpleStatement.class))).thenReturn(resultSet);
        try (MockedStatic<CassandraUtil> util = mockStatic(CassandraUtil.class)) {
            util.when(() -> CassandraUtil.createResponse(resultSet)).thenReturn(List.of(Map.of("id", "1")));
            List<Map<String, Object>> result = cassandraOperation.getRecordsByPropertiesWithoutFiltering(keyspace, table, propertyMap, fields);
            assertEquals(1, result.size());
        }
    }

    @Test
    void testUpdateRecordSuccess() {
        String keyspace = "ks";
        String table = "tbl";
        Map<String, Object> updateAttributes = Map.of("name", "newName");
        Map<String, Object> compositeKey = Map.of("id", "1");

        when(connectionManager.getSession(keyspace)).thenReturn(session);

        Map<String, Object> result = cassandraOperation.updateRecord(keyspace, table, updateAttributes, compositeKey);
        assertEquals(Constants.SUCCESS, result.get(Constants.RESPONSE));
    }

    @Test
    void testUpdateRecordFailure() {
        String keyspace = "ks";
        String table = "tbl";
        Map<String, Object> updateAttributes = Map.of("name", "newName");
        Map<String, Object> compositeKey = Map.of("id", "1");

        when(connectionManager.getSession(keyspace)).thenThrow(new RuntimeException("fail"));
        assertThrows(RuntimeException.class, () -> cassandraOperation.updateRecord(keyspace, table, updateAttributes, compositeKey));
    }
}