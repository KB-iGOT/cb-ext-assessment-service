package com.igot.cb.common.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SunbirdApiResultResponseTest {

    @Test
    void testGettersAndSetters() {
        SunbirdApiResultResponse response = new SunbirdApiResultResponse();

        int count = 5;
        List<SunbirdApiRespContent> contentList = Arrays.asList(new SunbirdApiRespContent(), new SunbirdApiRespContent());

        response.setCount(count);
        response.setContent(contentList);

        assertEquals(count, response.getCount());
        assertEquals(contentList, response.getContent());
    }

    @Test
    void testDefaultValues() {
        SunbirdApiResultResponse response = new SunbirdApiResultResponse();

        assertEquals(0, response.getCount());
        assertNull(response.getContent());
    }
}
