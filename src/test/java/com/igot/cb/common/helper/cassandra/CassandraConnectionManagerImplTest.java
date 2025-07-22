package com.igot.cb.common.helper.cassandra;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.igot.cb.common.util.Constants;
import com.igot.cb.common.util.PropertiesCache;
import com.igot.cb.core.exception.CustomException;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CassandraConnectionManagerImplTest {

    @Mock
    PropertiesCache propertiesCache;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetConsistencyLevel_valid() {
        try (MockedStatic<PropertiesCache> staticMock = mockStatic(PropertiesCache.class)) {
            staticMock.when(PropertiesCache::getInstance).thenReturn(propertiesCache);
            when(propertiesCache.readProperty(Constants.SUNBIRD_CASSANDRA_CONSISTENCY_LEVEL))
                    .thenReturn("LOCAL_QUORUM");

            ConsistencyLevel level = invokeGetConsistencyLevel();
            assertEquals(DefaultConsistencyLevel.LOCAL_QUORUM, level);
        }
    }

    @Test
    void testGetConsistencyLevel_invalid() {
        try (MockedStatic<PropertiesCache> staticMock = mockStatic(PropertiesCache.class)) {
            staticMock.when(PropertiesCache::getInstance).thenReturn(propertiesCache);
            when(propertiesCache.readProperty(Constants.SUNBIRD_CASSANDRA_CONSISTENCY_LEVEL))
                    .thenReturn("INVALID");

            ConsistencyLevel level = invokeGetConsistencyLevel();
            assertNull(level);
        }
    }

    @Test
    void testShutdownHook_closesSessions() throws Exception {
        // Arrange
        CqlSession mockSession1 = mock(CqlSession.class);
        CqlSession mockSession2 = mock(CqlSession.class);
        Map<String, CqlSession> sessionMap = getSessionMap();
        sessionMap.clear();
        sessionMap.put("ks1", mockSession1);
        sessionMap.put("ks2", mockSession2);

        // Act
        CassandraConnectionManagerImpl.ResourceCleanUp cleanup = new CassandraConnectionManagerImpl.ResourceCleanUp();
        cleanup.run();

        // Assert
        verify(mockSession1, times(1)).close();
        verify(mockSession2, times(1)).close();
    }

    private ConsistencyLevel invokeGetConsistencyLevel() {
        try {
            Method method = CassandraConnectionManagerImpl.class.getDeclaredMethod("getConsistencyLevel");
            method.setAccessible(true);
            return (ConsistencyLevel) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testConstructorThrowsException_whenHostIsBlank() {
        try (
                MockedStatic<PropertiesCache> propertiesCacheStatic = Mockito.mockStatic(PropertiesCache.class)
        ) {
            // Arrange
            PropertiesCache mockPropertiesCache = mock(PropertiesCache.class);
            propertiesCacheStatic.when(PropertiesCache::getInstance).thenReturn(mockPropertiesCache);
            when(mockPropertiesCache.getProperty(Constants.CASSANDRA_CONFIG_HOST)).thenReturn("");

            // Act & Assert
            CustomException exception = assertThrows(CustomException.class, CassandraConnectionManagerImpl::new);
            assertEquals("Cassandra host is not configured", exception.getMessage()); // Adjust message if needed
        }
    }

    @Test
    void testGetSession_returnsExistingSession() {
        CassandraConnectionManagerImpl manager = new CassandraConnectionManagerImpl();
        CqlSession mockSession = mock(CqlSession.class);
        when(mockSession.isClosed()).thenReturn(false);

        // Inject session into map
        String keyspace = "ks1";
        var map = getSessionMap();
        map.put(keyspace, mockSession);

        CqlSession result = manager.getSession(keyspace);
        assertSame(mockSession, result);
    }

    @Test
    void testRegisterShutDownHook_noException() {
        try (MockedStatic<Runtime> runtimeMock = mockStatic(Runtime.class)) {
            Runtime mockRuntime = mock(Runtime.class);
            runtimeMock.when(Runtime::getRuntime).thenReturn(mockRuntime);

            CassandraConnectionManagerImpl.registerShutDownHook();

            verify(mockRuntime, times(1)).addShutdownHook(any(Thread.class));
        }
    }

    @Test
    void testGetConsistencyLevel_blankProperty() {
        try (MockedStatic<PropertiesCache> staticMock = mockStatic(PropertiesCache.class)) {
            PropertiesCache cache = mock(PropertiesCache.class);
            staticMock.when(PropertiesCache::getInstance).thenReturn(cache);
            when(cache.readProperty(Constants.SUNBIRD_CASSANDRA_CONSISTENCY_LEVEL)).thenReturn("");
            assertNull(invokeGetConsistencyLevel());
        }
    }

    // Helper to access private static cassandraSessionMap
    @SuppressWarnings("unchecked")
    private Map<String, CqlSession> getSessionMap() {
        try {
            var field = CassandraConnectionManagerImpl.class.getDeclaredField("cassandraSessionMap");
            field.setAccessible(true);
            return (Map<String, CqlSession>) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
