package com.igot.cb.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConsumerConfigurationTest {

    @Test
    void testConsumerConfigs() {
        ConsumerConfiguration config = new ConsumerConfiguration();
        ReflectionTestUtils.setField(config, "kafkabootstrapAddress", "localhost:9092");
        ReflectionTestUtils.setField(config, "kafkaOffsetResetValue", "earliest");
        ReflectionTestUtils.setField(config, "kafkaMaxPollInterval", 300000);
        ReflectionTestUtils.setField(config, "kafkaMaxPollRecords", 500);
        ReflectionTestUtils.setField(config, "kafkaAutoCommitInterval", 1000);

        Map<String, Object> props = config.consumerConfigs();

        assertEquals("localhost:9092", props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(true, props.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        assertEquals("1000", props.get(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG));
        assertEquals(1000, props.get(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG));
        assertEquals("15000", props.get(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG));
        assertEquals(StringDeserializer.class, props.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertEquals(StringDeserializer.class, props.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
        assertEquals("earliest", props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertEquals(300000, props.get(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG));
        assertEquals(500, props.get(ConsumerConfig.MAX_POLL_RECORDS_CONFIG));
    }

    @Test
    void testConsumerFactoryAndListenerContainerFactory() {
        ConsumerConfiguration config = new ConsumerConfiguration();
        ReflectionTestUtils.setField(config, "kafkabootstrapAddress", "localhost:9092");
        ReflectionTestUtils.setField(config, "kafkaOffsetResetValue", "earliest");
        ReflectionTestUtils.setField(config, "kafkaMaxPollInterval", 300000);
        ReflectionTestUtils.setField(config, "kafkaMaxPollRecords", 500);
        ReflectionTestUtils.setField(config, "kafkaAutoCommitInterval", 1000);

        assertNotNull(config.consumerFactory());
        assertNotNull(config.kafkaListenerContainerFactory());
    }
}
