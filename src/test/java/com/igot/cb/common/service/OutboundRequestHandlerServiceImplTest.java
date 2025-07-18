package com.igot.cb.common.service;

import com.igot.cb.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboundRequestHandlerServiceImplTest {

    @InjectMocks
    private OutboundRequestHandlerServiceImpl service;

    @Mock
    private RestTemplate restTemplate;

    private final String uri = "http://test.com/api";
    private final Map<String, String> headers = Map.of("Authorization", "Bearer token");

    @Test
    void testFetchResult_Success() {
        Map<String, Object> expected = Map.of("key", "value");
        when(restTemplate.getForObject(uri, Map.class)).thenReturn(expected);

        Object result = service.fetchResult(uri);
        assertEquals(expected, result);
    }

    @Test
    void testFetchResult_HttpClientErrorException() throws Exception {
        String errorJson = "{\"error\":\"bad request\"}";
        when(restTemplate.getForObject(uri, Map.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", errorJson.getBytes(), null));

        Object result = service.fetchResult(uri);
        assertTrue(result instanceof Map);
        assertEquals("bad request", ((Map<?, ?>) result).get("error"));
    }

    @Test
    void testFetchUsingGetWithHeaders_Success() {
        Map<String, Object> expected = Map.of("foo", "bar");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(expected, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        Object result = service.fetchUsingGetWithHeaders(uri, headers);
        assertEquals(expected, result);
    }

    @Test
    void testFetchResultUsingPatch_Success() {
        Map<String, Object> expected = Map.of("patched", true);
        when(restTemplate.patchForObject(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(expected);

        Map<String, Object> result = service.fetchResultUsingPatch(uri, Map.of("a", 1), headers);
        assertEquals(expected, result);
    }

    @Test
    void testFetchResultUsingPatch_HttpClientErrorException() throws Exception {
        String errorJson = "{\"error\":\"patch failed\"}";
        when(restTemplate.patchForObject(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", errorJson.getBytes(), null));

        Map<String, Object> result = service.fetchResultUsingPatch(uri, Map.of("a", 1), headers);
        assertEquals("patch failed", result.get("error"));
    }

    @Test
    void testFetchResultUsingPost_Success() {
        Map<String, Object> expected = Map.of("created", true);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(expected);

        Map<String, Object> result = service.fetchResultUsingPost(uri, Map.of("b", 2), headers);
        assertEquals(expected, result);
    }

    @Test
    void testFetchResultUsingPost_HttpClientErrorException() throws Exception {
        String errorJson = "{\"error\":\"post failed\"}";
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", errorJson.getBytes(), null));

        Map<String, Object> result = service.fetchResultUsingPost(uri, Map.of("b", 2), headers);
        assertEquals("post failed", result.get("error"));
    }

    @Test
    void testFetchResultUsingGet_Success() {
        Map<String, Object> expected = Map.of("got", true);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(expected, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = service.fetchResultUsingGet(uri, headers);
        assertEquals(expected, result);
    }

    @Test
    void testFetchResultUsingGet_HttpClientErrorException() throws Exception {
        String errorJson = "{\"error\":\"get failed\"}";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", errorJson.getBytes(), null));

        Map<String, Object> result = service.fetchResultUsingGet(uri, headers);
        assertEquals("get failed", result.get("error"));
    }

    @Test
    void testFetchResult_Exception() {
        when(restTemplate.getForObject(uri, Map.class)).thenThrow(new RuntimeException("fail"));
        Object result = service.fetchResult(uri);
        assertNull(result);
    }

    @Test
    void testFetchUsingGetWithHeaders_HttpClientErrorException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        Object result = service.fetchUsingGetWithHeaders(uri, headers);
        assertNull(result);
    }

    @Test
    void testFetchUsingGetWithHeaders_Exception() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("fail"));
        Object result = service.fetchUsingGetWithHeaders(uri, headers);
        assertNull(result);
    }

    @Test
    void testFetchResultUsingPatch_NullResponse() {
        when(restTemplate.patchForObject(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(null);
        Map<String, Object> result = service.fetchResultUsingPatch(uri, Map.of("a", 1), headers);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchResultUsingPost_JsonProcessingException() throws Exception {
        // Simulate a response that causes JsonProcessingException in debug logging
        Map<String, Object> response = Map.of("invalid", new Object());
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(response);
        // Enable debug logging if possible, or just call the method
        Map<String, Object> result = service.fetchResultUsingPost(uri, Map.of("b", 2), headers);
        assertEquals(response, result);
    }

    @Test
    void testFetchResultUsingGet_JsonProcessingException() throws Exception {
        // Simulate a response that causes JsonProcessingException in debug logging
        Map<String, Object> response = Map.of("invalid", new Object());
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);
        Map<String, Object> result = service.fetchResultUsingGet(uri, headers);
        assertEquals(response, result);
    }
}
