package uk.gov.companieshouse.pscdataapi.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords;

import java.io.IOException;
import java.time.Duration;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import uk.gov.companieshouse.pscdataapi.config.TestKafkaConfig;
import uk.gov.companieshouse.pscdataapi.kafka.PscMergeKafkaProducer;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscmerge.PscMerge;

@SpringBootTest
@Testcontainers
@Import(TestKafkaConfig.class)
class PscMergeKafkaProducerIT {

    @Container
    protected static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:latest");

    @Autowired
    private KafkaConsumer<String, byte[]> testConsumer;
    @Autowired
    private PscMergeKafkaProducer kafkaProducer;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldSuccessfullyProduceToPscMergeTopic() throws IOException {
        // given
        DataMapHolder.get().requestId("contextId");
        PscMerge expected = new PscMerge("pscId", "previousPscId", "contextId");

        // when
        kafkaProducer.invokePscMerge("pscId", "previousPscId");

        // then
        ConsumerRecords<String, byte[]> records = getRecords(testConsumer, Duration.ofMillis(10000L), 1);
        byte[] actualBytes = records.records("psc-merge").iterator().next().value();
        Decoder decoder = DecoderFactory.get().binaryDecoder(actualBytes, null);
        DatumReader<PscMerge> reader = new ReflectDatumReader<>(PscMerge.class);
        PscMerge actual = reader.read(null, decoder);
        assertEquals(expected, actual);
    }
}