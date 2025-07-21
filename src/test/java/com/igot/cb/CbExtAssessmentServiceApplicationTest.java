package com.igot.cb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CbExtAssessmentServiceApplicationTest {

    private final CbExtAssessmentServiceApplication app = new CbExtAssessmentServiceApplication();

    @Test
    void restTemplateBean_ShouldNotBeNull() {
        RestTemplate restTemplate = app.restTemplate();

        assertNotNull(restTemplate, "RestTemplate should not be null");
        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
        assertNotNull(factory, "ClientHttpRequestFactory should not be null");
        assertTrue(factory.toString().contains("HttpComponentsClientHttpRequestFactory"),
                "Request factory should be an instance of HttpComponentsClientHttpRequestFactory");
    }
}
