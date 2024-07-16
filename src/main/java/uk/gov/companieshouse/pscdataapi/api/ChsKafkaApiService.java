package uk.gov.companieshouse.pscdataapi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
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
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.util.PscTransformationHelper;

@Service
public class ChsKafkaApiService {

    private static final String PSC_URI = "/company/%s/persons-with-significant-control/"
            + "%s/%s";
    private static final String CHANGED_EVENT_TYPE = "changed";
    private static final String DELETE_EVENT_TYPE = "deleted";

    private final InternalApiClient internalApiClient;
    private final Logger logger;

    @Value("${chs.api.kafka.url}")
    private String chsKafkaApiUrl;
    @Value("${chs.api.kafka.resource-changed.uri}")
    private String resourceChangedUri;

    public ChsKafkaApiService(InternalApiClient internalApiClient, Logger logger) {
        this.internalApiClient = internalApiClient;
        this.logger = logger;
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
                                                              String kind, PscData pscData) {
        internalApiClient.setBasePath(chsKafkaApiUrl);
        PrivateChangedResourcePost changedResourcePost =
                internalApiClient.privateChangedResourceHandler()
                        .postChangedResource(resourceChangedUri,
                                mapChangedResource(contextId, companyNumber,
                                        notificationId, kind, true, pscData));
        return handleApiCall(changedResourcePost);
    }

    private ChangedResource mapChangedResource(String contextId, String companyNumber,
                                               String notificationId,
                                               String kind, boolean isDelete, PscData pscData) {
        ChangedResourceEvent event = new ChangedResourceEvent();
        ChangedResource changedResource = new ChangedResource();
        event.setPublishedAt(String.valueOf(OffsetDateTime.now()));
        if (isDelete) {
            event.setType(DELETE_EVENT_TYPE);

            ObjectMapper secondObjectMapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            try {
                String pscDataAsString = secondObjectMapper.writeValueAsString(pscData);
                Object pscDataAsObject = secondObjectMapper.readValue(pscDataAsString, Object.class);
                changedResource.setDeletedData(pscDataAsObject);
            } catch (JsonProcessingException ex) {
                throw new SerDesException("Failed to serialise/deserialise psc data", ex);
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
}
