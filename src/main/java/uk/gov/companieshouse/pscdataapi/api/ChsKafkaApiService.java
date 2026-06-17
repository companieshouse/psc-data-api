package uk.gov.companieshouse.pscdataapi.api;

import java.time.Instant;
import static java.time.ZoneOffset.UTC;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;
import uk.gov.companieshouse.pscdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.models.PscDeleteRequest;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.StreamEventOutboxDocument;
import uk.gov.companieshouse.pscdataapi.repository.StreamEventOutboxRepository;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;
import uk.gov.companieshouse.pscdataapi.util.PscTransformationHelper;

@Service
public class ChsKafkaApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private static final String RESOURCE_CHANGED_URI = "/private/resource-changed";
    private static final String PSC_URI = "/company/%s/persons-with-significant-control/%s/%s";
    private static final String CHANGED_EVENT_TYPE = "changed";
    private static final String DELETE_EVENT_TYPE = "deleted";
    private static final DateTimeFormatter PUBLISHED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            .withZone(UTC);
    public static final String INDIVIDUAL_BENEFICIAL_OWNER = "individual-beneficial-owner";
    public static final String CORPORATE_ENTITY_BENEFICIAL_OWNER = "corporate-entity-beneficial-owner";
    public static final String LEGAL_PERSON_BENEFICIAL_OWNER = "legal-person-beneficial-owner";
    public static final String SUPER_SECURE_BENEFICIAL_OWNER = "super-secure-beneficial-owner";
    private static final int MAX_BACKOFF_SECONDS = 300;

    private final CompanyPscTransformer companyPscTransformer;
    private final Supplier<InternalApiClient> kafkaApiClientSupplier;
    private final ObjectMapper objectMapper;
    private final StreamEventOutboxRepository streamEventOutboxRepository;
    private final int outboxBatchSize;

    public ChsKafkaApiService(CompanyPscTransformer companyPscTransformer,
            @Qualifier("kafkaApiClientSupplier") Supplier<InternalApiClient> kafkaApiClientSupplier,
            ObjectMapper objectMapper,
            StreamEventOutboxRepository streamEventOutboxRepository,
            @Value("${stream.outbox.batch_size:50}") int outboxBatchSize) {
        this.companyPscTransformer = companyPscTransformer;
        this.kafkaApiClientSupplier = kafkaApiClientSupplier;
        this.objectMapper = objectMapper;
        this.streamEventOutboxRepository = streamEventOutboxRepository;
        this.outboxBatchSize = outboxBatchSize > 0 ? outboxBatchSize : 50;
    }

    @StreamEvents
    public ApiResponse<Void> invokeChsKafkaApi(String companyNumber, String notificationId, String kind) {
        ChangedResource changedResource = mapChangedResource(companyNumber, notificationId, kind, false, null);
        PrivateChangedResourcePost changedResourcePost =
                kafkaApiClientSupplier.get()
                        .privateChangedResourceHandler()
                .postChangedResource(RESOURCE_CHANGED_URI, changedResource);
        return handleApiCall(changedResourcePost, changedResource);
    }

    @StreamEvents
    public ApiResponse<Void> invokeChsKafkaApiWithDeleteEvent(PscDeleteRequest deleteRequest, PscDocument pscDocument) {
        ChangedResource changedResource = mapChangedResource(
                deleteRequest.companyNumber(),
                deleteRequest.notificationId(),
                deleteRequest.kind(),
                true,
                pscDocument
        );
        LOGGER.info("PSC delete stream payload: " + changedResource);
        PrivateChangedResourcePost changedResourcePost =
            kafkaApiClientSupplier.get()
                    .privateChangedResourceHandler()
                    .postChangedResource(RESOURCE_CHANGED_URI, changedResource);
        return handleApiCall(changedResourcePost, changedResource);
    }

    @Scheduled(fixedDelayString = "${stream.outbox.retry_interval_ms:30000}")
    public void replayOutboxEvents() {
        List<StreamEventOutboxDocument> pendingEvents = streamEventOutboxRepository
                .findByNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        Instant.now(),
                        PageRequest.of(0, outboxBatchSize)
                );

        for (StreamEventOutboxDocument event : pendingEvents) {
            try {
                ChangedResource changedResource = objectMapper.readValue(event.getPayload(), ChangedResource.class);
                PrivateChangedResourcePost changedResourcePost = kafkaApiClientSupplier.get()
                        .privateChangedResourceHandler()
                        .postChangedResource(RESOURCE_CHANGED_URI, changedResource);
                changedResourcePost.execute();
                streamEventOutboxRepository.delete(event);
                LOGGER.info("Successfully replayed outbox stream event", DataMapHolder.getLogMap());
            } catch (ApiErrorResponseException | RuntimeException ex) {
                recordReplayFailure(event, ex);
            } catch (JsonProcessingException ex) {
                recordReplayFailure(event, ex);
            }
        }
    }

    private ChangedResource mapChangedResource(String companyNumber, String notificationId,
            String kind, boolean isDelete, PscDocument pscDocument) {
        ChangedResourceEvent event = new ChangedResourceEvent();
        ChangedResource changedResource = new ChangedResource();
        event.setPublishedAt(PUBLISHED_AT_FORMAT.format(Instant.now()));
        if (isDelete) {
            event.setType(DELETE_EVENT_TYPE);
            if (pscDocument != null) {
                // This write-value/read-value is necessary to remove null fields during the jackson conversion
                try {
                    Object pscObject = switch (kind) {
                        case "individual-person-with-significant-control" ->
                                companyPscTransformer.transformPscDocToIndividual(pscDocument, false);
                        case INDIVIDUAL_BENEFICIAL_OWNER ->
                                companyPscTransformer.transformPscDocToIndividualBeneficialOwner(pscDocument, false);
                        case "corporate-entity-person-with-significant-control" ->
                                companyPscTransformer.transformPscDocToCorporateEntity(pscDocument);
                        case CORPORATE_ENTITY_BENEFICIAL_OWNER ->
                                companyPscTransformer.transformPscDocToCorporateEntityBeneficialOwner(pscDocument);
                        case "legal-person-person-with-significant-control" ->
                                companyPscTransformer.transformPscDocToLegalPerson(pscDocument);
                        case LEGAL_PERSON_BENEFICIAL_OWNER ->
                                companyPscTransformer.transformPscDocToLegalPersonBeneficialOwner(pscDocument);
                        case "super-secure-person-with-significant-control" ->
                                companyPscTransformer.transformPscDocToSuperSecure(pscDocument);
                        case SUPER_SECURE_BENEFICIAL_OWNER ->
                                companyPscTransformer.transformPscDocToSuperSecureBeneficialOwner(pscDocument);
                        default -> null;
                    };
                    changedResource.setDeletedData(deserializedData(pscObject));
                } catch (JsonProcessingException e) {
                    throw new SerDesException("Failed to serialise/deserialise psc data", e);
                }
            }
        } else {
            event.setType(CHANGED_EVENT_TYPE);
        }
        changedResource.setResourceUri(String.format(PSC_URI, companyNumber,
                mapKind(kind), notificationId));
        changedResource.event(event);
        changedResource.setResourceKind(PscTransformationHelper.mapResourceKind(kind));
        changedResource.setContextId(DataMapHolder.getRequestId());
        return changedResource;
    }

    private static String mapKind(String kind) {
        HashMap<String, String> kindMap = new HashMap<>();
        kindMap.put("individual-person-with-significant-control", "individual");
        kindMap.put("legal-person-person-with-significant-control", "legal-person");
        kindMap.put("corporate-entity-person-with-significant-control", "corporate-entity");
        kindMap.put("super-secure-person-with-significant-control", "super-secure");
        kindMap.put(INDIVIDUAL_BENEFICIAL_OWNER, INDIVIDUAL_BENEFICIAL_OWNER);
        kindMap.put(LEGAL_PERSON_BENEFICIAL_OWNER, LEGAL_PERSON_BENEFICIAL_OWNER);
        kindMap.put(CORPORATE_ENTITY_BENEFICIAL_OWNER, CORPORATE_ENTITY_BENEFICIAL_OWNER);
        kindMap.put(SUPER_SECURE_BENEFICIAL_OWNER, SUPER_SECURE_BENEFICIAL_OWNER);

        return kindMap.get(kind);
    }

    private ApiResponse<Void> handleApiCall(PrivateChangedResourcePost changedResourcePost, ChangedResource changedResource) {
        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException ex) {
            final String msg = "Unsuccessful call to resource-changed endpoint";
            LOGGER.error(msg, ex);
            saveToOutbox(changedResource, ex);
            throw new ServiceUnavailableException(msg);
        } catch (RuntimeException ex) {
            LOGGER.error("Error occurred while calling resource-changed endpoint", ex);
            saveToOutbox(changedResource, ex);
            throw ex;
        }
    }

    private void saveToOutbox(ChangedResource changedResource, Exception ex) {
        try {
            StreamEventOutboxDocument outboxDocument = new StreamEventOutboxDocument();
            outboxDocument.setPayload(objectMapper.writeValueAsString(changedResource));
            outboxDocument.setResourceUri(changedResource.getResourceUri());
            outboxDocument.setResourceKind(changedResource.getResourceKind());
            outboxDocument.setEventType(changedResource.getEvent() != null ? changedResource.getEvent().getType() : null);
            outboxDocument.setAttempts(0);
            outboxDocument.setCreatedAt(Instant.now());
            outboxDocument.setNextAttemptAt(Instant.now());
            outboxDocument.setLastError(ex.getMessage());
            streamEventOutboxRepository.save(outboxDocument);
        } catch (JsonProcessingException jsonEx) {
            LOGGER.error("Failed to serialise changed resource for outbox persistence", jsonEx);
        } catch (RuntimeException runtimeEx) {
            LOGGER.error("Failed to persist outbox stream event", runtimeEx);
        }
    }

    private void recordReplayFailure(StreamEventOutboxDocument event, Exception ex) {
        int attempts = event.getAttempts() + 1;
        event.setAttempts(attempts);
        event.setLastError(ex.getMessage());
        event.setNextAttemptAt(Instant.now().plus(calculateBackoffSeconds(attempts), ChronoUnit.SECONDS));
        streamEventOutboxRepository.save(event);
        LOGGER.error("Failed replaying outbox stream event", ex);
    }

    private long calculateBackoffSeconds(int attempts) {
        return Math.min(MAX_BACKOFF_SECONDS, 1L << Math.min(attempts, 20));
    }

    private Object deserializedData(Object pscDocument) throws JsonProcessingException {
        return objectMapper.readValue(objectMapper.writeValueAsString(pscDocument), Object.class);
    }
}
