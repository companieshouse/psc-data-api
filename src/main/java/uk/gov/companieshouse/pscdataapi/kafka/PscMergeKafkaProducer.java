package uk.gov.companieshouse.pscdataapi.kafka;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import java.util.concurrent.CompletionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.pscdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscmerge.PscMerge;

@Component
public class PscMergeKafkaProducer implements PscMergeProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final KafkaTemplate<String, PscMerge> kafkaTemplate;
    private final String pscMergeTopic;

    public PscMergeKafkaProducer(KafkaTemplate<String, PscMerge> kafkaTemplate,
            @Value("${kafka.psc-merge.topic}") String pscMergeTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.pscMergeTopic = pscMergeTopic;
    }

    public void invokePscMerge(String pscId, String previousPscId) {
        try {
            PscMerge pscMerge = new PscMerge(pscId, previousPscId, DataMapHolder.getRequestId());
            kafkaTemplate.send(pscMergeTopic, pscMerge).join();
        } catch (CompletionException ex) {
            final String msg = "Completion error during Kafka send Future";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new BadGatewayException(msg, ex);
        } catch (KafkaException ex) {
            final String msg = "Error publishing to psc-merge topic";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new BadGatewayException(msg, ex);
        }
        LOGGER.info("Successfully published message to psc-merge topic", DataMapHolder.getLogMap());
    }
}
