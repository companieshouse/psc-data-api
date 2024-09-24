package uk.gov.companieshouse.pscdataapi.api;

import static java.time.ZoneOffset.UTC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;
import uk.gov.companieshouse.pscdataapi.util.PscTransformationHelper;

@Service
public class ChsKafkaApiService {

    private static final String PSC_URI = "/company/%s/persons-with-significant-control/"
            + "%s/%s";
    private static final String CHANGED_EVENT_TYPE = "changed";
    private static final String DELETE_EVENT_TYPE = "deleted";
    private static final DateTimeFormatter PUBLISHED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss")
            .withZone(UTC);

    private final CompanyPscTransformer companyPscTransformer;
    private final InternalApiClient internalApiClient;
    private final Logger logger;
    private final ObjectMapper objectMapper;

    @Value("${chs.api.kafka.url}")
    private String chsKafkaApiUrl;
    @Value("${chs.api.kafka.resource-changed.uri}")
    private String resourceChangedUri;

    public ChsKafkaApiService(InternalApiClient internalApiClient, Logger logger,
                              ObjectMapper objectMapper, CompanyPscTransformer companyPscTransformer) {
        this.internalApiClient = internalApiClient;
        this.logger = logger;
        this.objectMapper = objectMapper;
        this.companyPscTransformer = companyPscTransformer;
    }

    /**
     * Creates a ChangedResource object to send a request to the chs kafka api.
     *
     * @param contextId      chs kafka id
     * @param companyNumber  company number of psc
     * @param notificationId mongo id
     * @return passes request to api response handling
     */

    @StreamEvents
    public ApiResponse<Void> invokeChsKafkaApi(String contextId, String companyNumber,
                                               String notificationId, String kind) {
        internalApiClient.setBasePath(chsKafkaApiUrl);
        PrivateChangedResourcePost changedResourcePost = internalApiClient
                .privateChangedResourceHandler().postChangedResource(resourceChangedUri,
                        mapChangedResource(contextId, companyNumber, notificationId, kind,
                                false, null));
        return handleApiCall(changedResourcePost);
    }

    /**
     * Creates a ChangedResource object to send a delete request to the chs kafka api.
     *
     * @param contextId      chs kafka id
     * @param companyNumber  company number of psc
     * @param notificationId mongo id
     * @return passes request to api response handling
     */
    @StreamEvents
    public ApiResponse<Void> invokeChsKafkaApiWithDeleteEvent(String contextId,
                                                              String companyNumber,
                                                              String notificationId,
                                                              String kind, PscDocument pscDocument) {
        internalApiClient.setBasePath(chsKafkaApiUrl);
        PrivateChangedResourcePost changedResourcePost =
                internalApiClient.privateChangedResourceHandler()
                        .postChangedResource(resourceChangedUri,
                                mapChangedResource(contextId, companyNumber,
                                        notificationId, kind, true, pscDocument));
        return handleApiCall(changedResourcePost);
    }

    private ChangedResource mapChangedResource(String contextId, String companyNumber,
                                               String notificationId,
                                               String kind, boolean isDelete, PscDocument pscDocument) {
        ChangedResourceEvent event = new ChangedResourceEvent();
        ChangedResource changedResource = new ChangedResource();
        event.setPublishedAt(PUBLISHED_AT_FORMAT.format(Instant.now()));
        if (isDelete) {
            event.setType(DELETE_EVENT_TYPE);
            if (pscDocument != null) {
                // This write-value/read-value is necessary to remove null fields during the jackson conversion
                try {
                    Object pscObject = switch (pscDocument.getData().getKind()) {
                        case "individual-person-with-significant-control" ->
                                companyPscTransformer.transformPscDocToIndividual(pscDocument, false);
                        case "individual-beneficial-owner" ->
                                companyPscTransformer.transformPscDocToIndividualBeneficialOwner(pscDocument, false);
                        case "corporate-entity-person-with-significant-control" ->
                                companyPscTransformer.transformPscDocToCorporateEntity(pscDocument);
                        case "corporate-entity-beneficial-owner" ->
                                companyPscTransformer.transformPscDocToCorporateEntityBeneficialOwner(pscDocument);
                        case "legal-person-person-with-significant-control" ->
                                companyPscTransformer.transformPscDocToLegalPerson(pscDocument);
                        case "legal-person-beneficial-owner" ->
                                companyPscTransformer.transformPscDocToLegalPersonBeneficialOwner(pscDocument);
                        case "super-secure-person-with-significant-control" ->
                                companyPscTransformer.transformPscDocToSuperSecure(pscDocument);
                        case "super-secure-beneficial-owner" ->
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
        changedResource.setContextId(contextId);
        return changedResource;
    }

    private static String mapKind(String kind) {
        HashMap<String, String> kindMap = new HashMap<>();
        kindMap.put("individual-person-with-significant-control", "individual");
        kindMap.put("legal-person-person-with-significant-control", "legal-person");
        kindMap.put("corporate-entity-person-with-significant-control", "corporate-entity");
        kindMap.put("super-secure-person-with-significant-control", "super-secure");
        kindMap.put("individual-beneficial-owner", "individual-beneficial-owner");
        kindMap.put("legal-person-beneficial-owner", "legal-person-beneficial-owner");
        kindMap.put("corporate-entity-beneficial-owner", "corporate-entity-beneficial-owner");
        kindMap.put("super-secure-beneficial-owner", "super-secure-beneficial-owner");

        return kindMap.get(kind);
    }

    private ApiResponse<Void> handleApiCall(PrivateChangedResourcePost changedResourcePost) {
        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException exception) {
            logger.error("Unsuccessful call to /resource-changed endpoint", exception);
            throw new ServiceUnavailableException(exception.getMessage());
        } catch (RuntimeException exception) {
            logger.error("Error occurred while calling /resource-changed endpoint", exception);
            throw exception;
        }
    }

    private Object deserializedData(Object pscDocument) throws JsonProcessingException {
        return objectMapper.readValue(objectMapper.writeValueAsString(pscDocument), Object.class);
    }
}
