package com.igot.cb.config;

import com.igot.cb.common.util.CbExtAssessmentServerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisConfigTest {

    private CbExtAssessmentServerProperties properties;
    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() {
        properties = mock(CbExtAssessmentServerProperties.class);
        when(properties.getRedisHostName()).thenReturn("localhost");
        when(properties.getRedisPort()).thenReturn("6379");
        when(properties.getRedisDataHostName()).thenReturn("localhost");
        when(properties.getRedisDataPort()).thenReturn("6380");
        when(properties.getMaxIdle()).thenReturn(10);
        when(properties.getMaxActive()).thenReturn(20);
        when(properties.getMinIdle()).thenReturn(5);
        when(properties.isTestOnBorrow()).thenReturn(true);
        when(properties.isTestOnReturn()).thenReturn(false);
        when(properties.isTestWhileIdle()).thenReturn(true);
        when(properties.getMinEvictableIdleTime()).thenReturn(60000L);
        when(properties.getTimeBetweenEvictionRuns()).thenReturn(30000L);
        when(properties.getNumTestsPerEvictionRun()).thenReturn(3);
        when(properties.isBlockWhenExhausted()).thenReturn(true);

        redisConfig = new RedisConfig();
        redisConfig.cbExtAssessmentServerProperties = properties;
    }

    @Test
    void testJedisPoolBean() {
        JedisPool pool = redisConfig.jedisPool();
        assertNotNull(pool);
    }

    @Test
    void testJedisDataPopulationPoolBean() {
        JedisPool pool = redisConfig.jedisDataPopulationPool();
        assertNotNull(pool);
    }

    /**
     * With the flag on, a missing username must fail at bean creation rather than on the first Redis
     * call. An unset property reads as "" rather than null, and Jedis sends the two-argument AUTH
     * whenever the username is non-null - so without this guard the server answers WRONGPASS on every
     * command and the cache layers swallow it as a miss.
     */
    @Test
    void testJedisPoolFailsWhenUsernameRequiredButMissing() {
        when(properties.isRedisPasswordRequired()).thenReturn(true);
        when(properties.getRedisUsername()).thenReturn("");
        when(properties.getRedisPassword()).thenReturn("cache-secret");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> redisConfig.jedisPool());
        assertTrue(ex.getMessage().contains("username"), ex.getMessage());
        // the message must point at the cache instance, not the data one
        assertTrue(ex.getMessage().contains("localhost:6379"), ex.getMessage());
    }

    @Test
    void testJedisPoolFailsWhenPasswordRequiredButMissing() {
        when(properties.isRedisPasswordRequired()).thenReturn(true);
        when(properties.getRedisUsername()).thenReturn("cache-user");
        when(properties.getRedisPassword()).thenReturn("  ");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> redisConfig.jedisPool());
        assertTrue(ex.getMessage().contains("password"), ex.getMessage());
        assertTrue(ex.getMessage().contains("localhost:6379"), ex.getMessage());
    }

    /**
     * An absent key reads as null rather than "". Both spellings of "not configured" are rejected.
     */
    @Test
    void testJedisPoolFailsWhenUsernameIsNull() {
        when(properties.isRedisPasswordRequired()).thenReturn(true);
        when(properties.getRedisUsername()).thenReturn(null);
        when(properties.getRedisPassword()).thenReturn("cache-secret");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> redisConfig.jedisPool());
        assertTrue(ex.getMessage().contains("username"), ex.getMessage());
    }

    /** The same two guards on the data pool, which is configured independently. */
    @Test
    void testJedisDataPopulationPoolFailsWhenUsernameRequiredButMissing() {
        when(properties.isRedisDataPasswordRequired()).thenReturn(true);
        when(properties.getRedisDataUsername()).thenReturn("");
        when(properties.getRedisDataPassword()).thenReturn("data-secret");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> redisConfig.jedisDataPopulationPool());
        assertTrue(ex.getMessage().contains("username"), ex.getMessage());
        // and at the data instance - the port is what tells the two apart
        assertTrue(ex.getMessage().contains("localhost:6380"), ex.getMessage());
    }

    @Test
    void testJedisDataPopulationPoolFailsWhenPasswordRequiredButMissing() {
        when(properties.isRedisDataPasswordRequired()).thenReturn(true);
        when(properties.getRedisDataUsername()).thenReturn("data-user");
        when(properties.getRedisDataPassword()).thenReturn("  ");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> redisConfig.jedisDataPopulationPool());
        assertTrue(ex.getMessage().contains("password"), ex.getMessage());
        assertTrue(ex.getMessage().contains("localhost:6380"), ex.getMessage());
    }

    @Test
    void testBuildPoolConfig() throws Exception {
        java.lang.reflect.Method method = RedisConfig.class.getDeclaredMethod("buildPoolConfig");
        method.setAccessible(true);
        JedisPoolConfig config = (JedisPoolConfig) method.invoke(redisConfig);

        assertEquals(20, config.getMaxTotal());
        assertEquals(5, config.getMinIdle());
        assertTrue(config.getTestOnBorrow());
        assertFalse(config.getTestOnReturn());
        assertTrue(config.getTestWhileIdle());
        assertEquals(60000L, config.getMinEvictableIdleTimeMillis());
        assertEquals(30000L, config.getTimeBetweenEvictionRunsMillis());
        assertEquals(3, config.getNumTestsPerEvictionRun());
        assertTrue(config.getBlockWhenExhausted());
    }
}
