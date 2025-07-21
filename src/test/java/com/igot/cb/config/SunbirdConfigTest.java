package com.igot.cb.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SunbirdConfigTest {

    @InjectMocks
    private SunbirdConfig sunbirdConfig;

    @Mock
    private CqlSession mockSession;


    private SunbirdConfig config;

    @BeforeEach
    void setUp() {
        // Create a concrete instance with overridden config values
        config = new SunbirdConfig() {
            @Override
            public String getContactPoints() {
                return "127.0.0.1";
            }

            @Override
            public int getPort() {
                return 9042;
            }

            @Override
            public String getLocalDataCenter() {
                return "datacenter1";
            }

            @Override
            public String getKeyspaceName() {
                return "test_keyspace";
            }

            @Override
            public CassandraConverter cassandraConverter() {
                return mock(CassandraConverter.class);
            }
        };
        ReflectionTestUtils.setField(sunbirdConfig, "contactPoints", "localhost");
        ReflectionTestUtils.setField(sunbirdConfig, "port", 9042);
        ReflectionTestUtils.setField(sunbirdConfig, "keyspaceName", "sunbird");
        ReflectionTestUtils.setField(sunbirdConfig, "sunbirdUser", "user");
        ReflectionTestUtils.setField(sunbirdConfig, "sunbirdPassword", "password");
    }

    @Test
    void cassandraTemplate() {
        // Arrange
        SunbirdConfig spyConfig = spy(sunbirdConfig);

        // Mock dependencies
        CassandraConverter mockConverter = mock(CassandraConverter.class);
        CassandraMappingContext mockMappingContext = mock(CassandraMappingContext.class);
        SpelAwareProxyProjectionFactory mockProjectionFactory = new SpelAwareProxyProjectionFactory();

        // Return the mocks when required
        when(mockConverter.getMappingContext()).thenReturn(mockMappingContext);
        when(mockConverter.getProjectionFactory()).thenReturn(mockProjectionFactory); // ✅ fix

        // Spy config to return our mocked converter
        doReturn(mockConverter).when(spyConfig).cassandraConverter();

        // Act
        CassandraAdminTemplate template = spyConfig.cassandraTemplate(mockSession);

        // Assert
        assertNotNull(template);
    }

    @Test
    void getKeyspaceName() {
        // Act
        String keyspaceName = sunbirdConfig.getKeyspaceName();

        // Assert
        assertEquals("sunbird", keyspaceName);
    }

    @Test
    void getPort() {
        // Act
        int port = sunbirdConfig.getPort();

        // Assert
        assertEquals(9042, port);
    }

    @Test
    void getContactPoints() {
        // Act
        String contactPoints = sunbirdConfig.getContactPoints();

        // Assert
        assertEquals("localhost", contactPoints);
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // Check parent class
            } catch (Exception e) {
                throw new RuntimeException("Failed to set field " + fieldName, e);
            }
        }
        throw new RuntimeException("Field '" + fieldName + "' not found in class hierarchy");
    }
}
