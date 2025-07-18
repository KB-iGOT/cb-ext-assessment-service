package com.igot.cb.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class SunbirdConfigTest {

    private SunbirdConfig sunbirdConfig;

    @BeforeEach
    void setUp() {
        sunbirdConfig = new SunbirdConfig();
        sunbirdConfig.setContactPoints("127.0.0.1");
        sunbirdConfig.setPort(9042);
        sunbirdConfig.setKeyspaceName("test_keyspace");
        try {
            var userField = SunbirdConfig.class.getDeclaredField("sunbirdUser");
            userField.setAccessible(true);
            userField.set(sunbirdConfig, "user");
            var passField = SunbirdConfig.class.getDeclaredField("sunbirdPassword");
            passField.setAccessible(true);
            passField.set(sunbirdConfig, "password");
        } catch (Exception e) {
            fail("Failed to set sunbirdUser or sunbirdPassword: " + e.getMessage());
        }
    }

    @Test
    void testCqlSessionBean() {
        CqlSession session = sunbirdConfig.cqlSession();
        assertNotNull(session);
        assertEquals("test_keyspace", session.getKeyspace().get().asInternal());
        session.close();
    }
}
