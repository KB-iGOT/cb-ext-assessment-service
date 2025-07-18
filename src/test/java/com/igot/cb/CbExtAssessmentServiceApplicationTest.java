package com.igot.cb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CbExtAssessmentServiceApplicationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void testMain() {
        CbExtAssessmentServiceApplication.main(new String[]{"test"});
    }

    @Test
    void testRestTemplateBean() {
        Assertions.assertNotNull(restTemplate);
        Assertions.assertTrue(restTemplate.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory);
    }
}
