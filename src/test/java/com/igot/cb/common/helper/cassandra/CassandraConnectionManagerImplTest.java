package com.igot.cb.common.helper.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.igot.cb.common.util.Constants;
import com.igot.cb.common.util.PropertiesCache;
import com.igot.cb.core.exception.CustomException;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CassandraConnectionManagerImplTest {

    private CassandraConnectionManagerImpl manager;
    private CqlSession session;
    private Metadata metadata;
    private KeyspaceMetadata keyspaceMetadata;
    private TableMetadata tableMetadata;
    private MockedStatic<PropertiesCache> propertiesCacheMock;

    @BeforeEach
    void setUp() {
        manager = spy(new CassandraConnectionManagerImpl());
        session = mock(CqlSession.class);
        metadata = mock(Metadata.class);
        keyspaceMetadata = mock(KeyspaceMetadata.class);
        tableMetadata = mock(TableMetadata.class);

        // Mock static PropertiesCache
        propertiesCacheMock = mockStatic(PropertiesCache.class);
        PropertiesCache cache = mock(PropertiesCache.class);
        propertiesCacheMock.when(PropertiesCache::getInstance).thenReturn(cache);
        when(cache.getProperty(Constants.CASSANDRA_CONFIG_HOST)).thenReturn("127.0.0.1");
        when(cache.getProperty(Constants.CORE_CONNECTIONS_PER_HOST_FOR_LOCAL)).thenReturn("1");
        when(cache.getProperty(Constants.CORE_CONNECTIONS_PER_HOST_FOR_REMOTE)).thenReturn("1");
        when(cache.getProperty(Constants.HEARTBEAT_INTERVAL)).thenReturn("30");
        when(cache.readProperty(Constants.SUNBIRD_CASSANDRA_CONSISTENCY_LEVEL)).thenReturn("LOCAL_ONE");
    }

    @AfterEach
    void tearDown() {
        if (propertiesCacheMock != null) {
            propertiesCacheMock.close(); // Close the static mock
        }
    }

    @Test
    void testGetTableList_Success() {
        // Set up static session field
        setStaticSessionField(manager, session);

        when(session.getMetadata()).thenReturn(metadata);
        when(metadata.getKeyspace("ks1")).thenReturn(Optional.of(keyspaceMetadata));
        Map<com.datastax.oss.driver.api.core.CqlIdentifier, TableMetadata> tables = new HashMap<>();
        com.datastax.oss.driver.api.core.CqlIdentifier id = com.datastax.oss.driver.api.core.CqlIdentifier.fromInternal("table1");
        tables.put(id, tableMetadata);
        when(keyspaceMetadata.getTables()).thenReturn(tables);

        List<String> result = manager.getTableList("ks1");
        assertEquals(List.of("table1"), result);
    }

    @Test
    void testGetTableList_KeyspaceNotFound() {
        setStaticSessionField(manager, session);
        when(session.getMetadata()).thenReturn(metadata);
        when(metadata.getKeyspace("ks1")).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () -> manager.getTableList("ks1"));
        assertTrue(ex.getMessage().contains("Keyspace not found"));
    }

    @Test
    void testRegisterShutDownHook() {
        // Just call to ensure no exceptions
        CassandraConnectionManagerImpl.registerShutDownHook();
    }

    // Helper to set private static session field
    private static void setStaticSessionField(CassandraConnectionManagerImpl manager, CqlSession session) {
        try {
            java.lang.reflect.Field field = CassandraConnectionManagerImpl.class.getDeclaredField("session");
            field.setAccessible(true);
            field.set(null, session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetSession_NewAndExisting() {
        // First call: simulate new session by clearing the map
        setSessionMapEntry("ks1", session);
        doReturn(false).when(session).isClosed();
        CqlSession s1 = manager.getSession("ks1");
        assertEquals(session, s1);

        // Second call: should return cached session
        CqlSession s2 = manager.getSession("ks1");
        assertEquals(session, s2);
    }
    @Test
    void testGetSession_ClosedSessionCreatesNew() {
        // Simulate a closed session
        CqlSession closedSession = mock(CqlSession.class);
        doReturn(true).when(closedSession).isClosed();
        setSessionMapEntry("ks2", closedSession);

        // Simulate a new session creation by replacing the closed session
        CqlSession newSession = mock(CqlSession.class);
        setSessionMapEntry("ks2", newSession);

        // Now getSession should return the new session
        CqlSession s = manager.getSession("ks2");
        assertEquals(newSession, s);
    }

    @Test
    void testCreateCassandraConnectionWithKeySpaces_HostBlank() {
        PropertiesCache cache = PropertiesCache.getInstance();
        when(cache.getProperty(Constants.CASSANDRA_CONFIG_HOST)).thenReturn("");
        CustomException ex = assertThrows(CustomException.class,
                () -> manager.getSession("ks3"));
        assertTrue(ex.getMessage().contains("Cassandra host is not configured"));
    }

    @Test
    void testGetConsistencyLevel_ValidAndInvalid() {
        propertiesCacheMock.when(() -> PropertiesCache.getInstance().readProperty(Constants.SUNBIRD_CASSANDRA_CONSISTENCY_LEVEL))
                .thenReturn("LOCAL_ONE");
        assertEquals(ConsistencyLevel.LOCAL_ONE, invokeGetConsistencyLevel());

        propertiesCacheMock.when(() -> PropertiesCache.getInstance().readProperty(Constants.SUNBIRD_CASSANDRA_CONSISTENCY_LEVEL))
                .thenReturn("INVALID");
        assertNull(invokeGetConsistencyLevel());

        propertiesCacheMock.when(() -> PropertiesCache.getInstance().readProperty(Constants.SUNBIRD_CASSANDRA_CONSISTENCY_LEVEL))
                .thenReturn("");
        assertNull(invokeGetConsistencyLevel());
    }

    @Test
    void testResourceCleanUp() throws Exception {
        // Add sessions to map
        CqlSession s1 = mock(CqlSession.class);
        CqlSession s2 = mock(CqlSession.class);
        setSessionMapEntry("ks1", s1);
        setSessionMapEntry("ks2", s2);
        setStaticSessionField(manager, s2);

        CassandraConnectionManagerImpl.ResourceCleanUp cleanup = new CassandraConnectionManagerImpl.ResourceCleanUp();
        cleanup.run();

        verify(s1, atLeast(0)).close();
        verify(s2, atLeast(1)).close();
    }

    // Helper to set static cassandraSessionMap
    private static void setSessionMapEntry(String key, CqlSession session) {
        try {
            java.lang.reflect.Field field = CassandraConnectionManagerImpl.class.getDeclaredField("cassandraSessionMap");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, CqlSession> map = (Map<String, CqlSession>) field.get(null);
            map.put(key, session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to invoke private static getConsistencyLevel
    private static ConsistencyLevel invokeGetConsistencyLevel() {
        try {
            java.lang.reflect.Method m = CassandraConnectionManagerImpl.class.getDeclaredMethod("getConsistencyLevel");
            m.setAccessible(true);
            return (ConsistencyLevel) m.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
