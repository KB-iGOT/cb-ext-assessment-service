package com.igot.cb.common.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SunbirdApiHierarchyResultBatchTest {

    @Test
    void testGettersAndSetters() {
        SunbirdApiHierarchyResultBatch batch = new SunbirdApiHierarchyResultBatch();

        int count = 5;
        List<String> participants = Arrays.asList("user1", "user2", "user3");

        batch.setCount(count);
        batch.setParticipants(participants);

        assertEquals(count, batch.getCount());
        assertEquals(participants, batch.getParticipants());
    }

    @Test
    void testDefaultValues() {
        SunbirdApiHierarchyResultBatch batch = new SunbirdApiHierarchyResultBatch();

        assertEquals(0, batch.getCount());
        assertNull(batch.getParticipants());
    }
}
