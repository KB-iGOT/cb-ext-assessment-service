package com.igot.cb.core.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

class ProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private Producer producer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPush() throws JsonProcessingException {
        String topic = "test-topic";
        Object value = new TestObj("foo", 123);

        producer.push(topic, value);

        String expectedMessage = new ObjectMapper().writeValueAsString(value);
        verify(kafkaTemplate, times(1)).send(topic, expectedMessage);
    }

    @Test
    void testPushWithKey() throws JsonProcessingException {
        String topic = "test-topic";
        String key = "key1";
        Object value = new TestObj("bar", 456);

        producer.pushWithKey(topic, value, key);

        String expectedMessage = new ObjectMapper().writeValueAsString(value);
        verify(kafkaTemplate, times(1)).send(topic, key, expectedMessage);
    }

    // Simple POJO for testing
    static class TestObj {
        public String name;
        public int id;

        public TestObj(String name, int id) {
            this.name = name;
            this.id = id;
        }
    }
}
