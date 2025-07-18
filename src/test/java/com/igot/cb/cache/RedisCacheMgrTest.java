package com.igot.cb.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.common.util.CbExtAssessmentServerProperties;
import com.igot.cb.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisCacheMgrTest {

    @InjectMocks
    private RedisCacheMgr redisCacheMgr;

    @Mock
    private JedisPool jedisPool;

    @Mock
    private JedisPool jedisDataPopulationPool;

    @Mock
    private CbExtAssessmentServerProperties cbExtAssessmentServerProperties;

    @Mock
    private Jedis jedis;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedisDataPopulationPool.getResource()).thenReturn(jedis);
        when(cbExtAssessmentServerProperties.getRedisQuestionsReadTimeOut()).thenReturn(84600);
        when(cbExtAssessmentServerProperties.getRedisTimeout()).thenReturn("84600");
        redisCacheMgr.postConstruct();
    }

    @Test
    void testPutAndGetCache() throws Exception {
        String key = "testKey";
        String value = "testValue";
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(value);

        when(jedis.set(anyString(), anyString())).thenReturn("OK");
        when(jedis.expire(anyString(), anyInt())).thenReturn(1L);
        when(jedis.get(Constants.REDIS_COMMON_KEY + key)).thenReturn(json);

        redisCacheMgr.putCache(key, value);
        String result = redisCacheMgr.getCache(key);
        assertEquals(json, result);
    }

    @Test
    void testPutStringInCacheAndGet() {
        String key = "strKey";
        String value = "strValue";
        when(jedis.set(anyString(), anyString())).thenReturn("OK");
        when(jedis.expire(anyString(), anyInt())).thenReturn(1L);
        when(jedis.get(Constants.REDIS_COMMON_KEY + key)).thenReturn(value);

        redisCacheMgr.putStringInCache(key, value);
        String result = redisCacheMgr.getCache(key);
        assertEquals(value, result);
    }

    @Test
    void testDeleteKeyByName() {
        String key = "delKey";
        when(jedis.del(Constants.REDIS_COMMON_KEY + key)).thenReturn(1L);

        boolean deleted = redisCacheMgr.deleteKeyByName(key);
        assertTrue(deleted);
    }

    @Test
    void testKeyExists() {
        String key = "existsKey";
        when(jedis.exists(Constants.REDIS_COMMON_KEY + key)).thenReturn(true);

        assertTrue(redisCacheMgr.keyExists(key));
    }

    @Test
    void testDeleteAllCBExtKey() {
        Set<String> keys = new HashSet<>(Arrays.asList(Constants.REDIS_COMMON_KEY + "a", Constants.REDIS_COMMON_KEY + "b"));
        when(jedis.keys(Constants.REDIS_COMMON_KEY + "*")).thenReturn(keys);
        when(jedis.del(anyString())).thenReturn(1L);

        assertTrue(redisCacheMgr.deleteAllCBExtKey());
    }

    @Test
    void testGetAllKeyNames() {
        Set<String> keys = new HashSet<>(Arrays.asList(Constants.REDIS_COMMON_KEY + "a", Constants.REDIS_COMMON_KEY + "b"));
        when(jedis.keys(Constants.REDIS_COMMON_KEY + "*")).thenReturn(keys);

        Set<String> result = redisCacheMgr.getAllKeyNames();
        assertEquals(keys, result);
    }

    @Test
    void testGetAllKeysAndValues() {
        Set<String> keys = Set.of(Constants.REDIS_COMMON_KEY + "a");
        when(jedis.keys(anyString())).thenReturn(keys);
        when(jedis.get(anyString())).thenReturn("val");
        List<Map<String, Object>> result = redisCacheMgr.getAllKeysAndValues();
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).containsKey(Constants.REDIS_COMMON_KEY + "a"));
    }

    @Test
    void testGetAllKeysAndValues_Exception() {
        when(jedis.keys(anyString())).thenThrow(new RuntimeException("fail"));
        List<Map<String, Object>> result = redisCacheMgr.getAllKeysAndValues();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetHashedCacheFromDataRedis() {
        when(jedisDataPopulationPool.getResource()).thenReturn(jedis);
        when(jedis.hget(anyString(), anyString())).thenReturn("hashVal");
        String val = redisCacheMgr.getHashedCacheFromDataRedis("key", 1, "field");
        assertEquals("hashVal", val);
    }

    @Test
    void testGetHashedCacheFromDataRedis_Exception() {
        when(jedisDataPopulationPool.getResource()).thenThrow(new RuntimeException("fail"));
        String val = redisCacheMgr.getHashedCacheFromDataRedis("key", 1, "field");
        assertNull(val);
    }

    @Test
    void testGetCacheFromDataRedish() {
        when(jedisDataPopulationPool.getResource()).thenReturn(jedis);
        when(jedis.get(anyString())).thenReturn("dataVal");
        String val = redisCacheMgr.getCacheFromDataRedish("key", 1);
        assertEquals("dataVal", val);
    }

    @Test
    void testGetCacheFromDataRedish_Exception() {
        when(jedisDataPopulationPool.getResource()).thenThrow(new RuntimeException("fail"));
        String val = redisCacheMgr.getCacheFromDataRedish("key", 1);
        assertNull(val);
    }

    @Test
    void testMget() {
        List<String> fields = List.of("1", "2");
        when(jedis.mget(any(String[].class))).thenReturn(List.of("a", "b"));
        List<String> result = redisCacheMgr.mget(fields);
        assertEquals(List.of("a", "b"), result);
    }

    @Test
    void testMget_Exception() {
        when(jedis.mget(any(String[].class))).thenThrow(new RuntimeException("fail"));
        List<String> result = redisCacheMgr.mget(List.of("1"));
        assertNull(result);
    }

    @Test
    void testPutCacheAsStringArray() {
        when(jedis.sadd(anyString(), any(String[].class))).thenReturn(1L);
        when(jedis.expire(anyString(), anyInt())).thenReturn(1L);
        redisCacheMgr.putCacheAsStringArray("key", new String[]{"a", "b"}, 100);
        // No exception means pass
    }

    @Test
    void testPutInQuestionCache() {
        when(jedis.set(anyString(), anyString())).thenReturn("OK");
        when(jedis.expire(anyString(), anyInt())).thenReturn(1L);
        redisCacheMgr.putInQuestionCache("key", "val");
    }

    @Test
    void testGetCacheWithIndex() {
        when(jedis.get(anyString())).thenReturn("val");
        String val = redisCacheMgr.getCache("key", 1);
        assertEquals("val", val);
    }

    @Test
    void testGetCacheWithIndex_Exception() {
        when(jedis.get(anyString())).thenThrow(new RuntimeException("fail"));
        String val = redisCacheMgr.getCache("key", 1);
        assertNull(val);
    }

    @Test
    void testHget() {
        when(jedisDataPopulationPool.getResource()).thenReturn(jedis);
        when(jedis.hmget(anyString(), any(String[].class))).thenReturn(List.of("v1", "v2"));
        List<String> result = redisCacheMgr.hget("key", 1, "f1", "f2");
        assertEquals(List.of("v1", "v2"), result);
    }

    @Test
    void testGetSetFromCacheAsCommaSeparated() {
        when(jedis.smembers(anyString())).thenReturn(Set.of("a", "b"));
        Set<String> result = redisCacheMgr.getSetFromCacheAsCommaSeparated("key");
        assertEquals(Set.of("a", "b"), result);
    }

    @Test
    void testValueExists() {
        when(jedis.sismember(anyString(), anyString())).thenReturn(true);
        assertTrue(redisCacheMgr.valueExists("key", "val"));
    }

    @Test
    void testGetContentFromCache() {
        when(jedis.get(anyString())).thenReturn("content");
        String val = redisCacheMgr.getContentFromCache("key");
        assertEquals("content", val);
    }
}
