package uk.gov.companieshouse.pscdataapi.kafka;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import uk.gov.companieshouse.pscdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscmerge.PscMerge;

@ExtendWith(MockitoExtension.class)
class PscMergeKafkaProducerTest {

    private static final String PSC_ID = "psc_id";
    private static final String PREVIOUS_PSC_ID = "previous_psc_id";
    private static final String CONTEXT_ID = "context_id";
    private static final String PSC_MERGE_TOPIC = "psc-merge";

    @Mock
    private KafkaTemplate<String, PscMerge> kafkaTemplate;

    private PscMergeKafkaProducer pscMergeKafkaProducer;

    @Mock
    private SendResult<String, PscMerge> sendResult;

    @BeforeEach
    void setup() {
        pscMergeKafkaProducer = new PscMergeKafkaProducer(kafkaTemplate, PSC_MERGE_TOPIC);
    }

    @Test
    void shouldInvokePscMerge() {
        // given
        PscMerge pscMerge = new PscMerge(PSC_ID, PREVIOUS_PSC_ID, CONTEXT_ID);

        DataMapHolder.get().requestId(CONTEXT_ID);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(CompletableFuture.completedFuture(sendResult));

        // when
        pscMergeKafkaProducer.invokePscMerge(PSC_ID, PREVIOUS_PSC_ID);

        // then
        verify(kafkaTemplate).send(PSC_MERGE_TOPIC, pscMerge);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenKafkaExceptionCaught() {
        // given
        PscMerge pscMerge = new PscMerge(PSC_ID, PREVIOUS_PSC_ID, CONTEXT_ID);

        DataMapHolder.get().requestId(CONTEXT_ID);
        when(kafkaTemplate.send(anyString(), any())).thenThrow(KafkaException.class);

        // when
        Executable executable = () -> pscMergeKafkaProducer.invokePscMerge(PSC_ID, PREVIOUS_PSC_ID);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(kafkaTemplate).send(PSC_MERGE_TOPIC, pscMerge);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenCompletableFutureFails() {
        // given
        PscMerge pscMerge = new PscMerge(PSC_ID, PREVIOUS_PSC_ID, CONTEXT_ID);

        DataMapHolder.get().requestId(CONTEXT_ID);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));

        // when
        Executable executable = () -> pscMergeKafkaProducer.invokePscMerge(PSC_ID, PREVIOUS_PSC_ID);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(kafkaTemplate).send(PSC_MERGE_TOPIC, pscMerge);
    }
}