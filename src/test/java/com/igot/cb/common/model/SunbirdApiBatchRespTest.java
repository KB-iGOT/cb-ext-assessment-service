package com.igot.cb.common.model;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SunbirdApiBatchRespTest {

    @Test
    void testGettersAndSetters() {
        SunbirdApiBatchResp resp = new SunbirdApiBatchResp();

        List<String> createdFor = Arrays.asList("user1", "user2");
        String endDate = "2024-12-31";
        String name = "Batch Name";
        String batchId = "batch123";
        String enrollmentType = "open";
        String enrollmentEndDate = "2024-12-15";
        String startDate = "2024-01-01";
        int status = 1;

        resp.setCreatedFor(createdFor);
        resp.setEndDate(endDate);
        resp.setName(name);
        resp.setBatchId(batchId);
        resp.setEnrollmentType(enrollmentType);
        resp.setEnrollmentEndDate(enrollmentEndDate);
        resp.setStartDate(startDate);
        resp.setStatus(status);

        assertEquals(createdFor, resp.getCreatedFor());
        assertEquals(endDate, resp.getEndDate());
        assertEquals(name, resp.getName());
        assertEquals(batchId, resp.getBatchId());
        assertEquals(enrollmentType, resp.getEnrollmentType());
        assertEquals(enrollmentEndDate, resp.getEnrollmentEndDate());
        assertEquals(startDate, resp.getStartDate());
        assertEquals(status, resp.getStatus());
    }

    @Test
    void testBatchAttributes() {
        SunbirdApiBatchResp resp = new SunbirdApiBatchResp();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", 123);

        resp.setBatchAttributes(attributes);

        assertEquals(attributes, resp.getBatchAttributes());
        assertEquals("value1", resp.getBatchAttributes().get("key1"));
        assertEquals(123, resp.getBatchAttributes().get("key2"));
    }
}
