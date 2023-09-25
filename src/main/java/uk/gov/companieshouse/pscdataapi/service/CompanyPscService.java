package uk.gov.companieshouse.pscdataapi.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.models.Created;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.repository.CompanyPscRepository;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;



@Service
public class CompanyPscService {

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");

    @Autowired
    private Logger logger;
    @Autowired
    private CompanyPscTransformer transformer;
    @Autowired
    private CompanyPscRepository repository;
    @Autowired
    ChsKafkaApiService chsKafkaApiService;
    @Autowired
    InternalApiClient internalApiClient;

    /**
     * Save or update a natural disqualification.
     * @param contextId     Id used for chsKafkaCall.
     * @param requestBody   Data to be saved.
     */
    @Transactional
    public void insertPscRecord(String contextId, FullRecordCompanyPSCApi requestBody)
            throws ServiceUnavailableException, BadRequestException {

        String notificationId;
        try {
            notificationId = requestBody.getExternalData().getNotificationId();
        } catch (NullPointerException ex) {
            throw new BadRequestException("NotificationId not provided");
        }
        boolean isLatestRecord = isLatestRecord(notificationId, requestBody
                .getInternalData().getDeltaAt());
        if (isLatestRecord) {
            PscDocument document = transformer.transformPsc(notificationId, requestBody);

            save(contextId, notificationId, document);
            chsKafkaApiService.invokeChsKafkaApi(contextId,
                    requestBody.getExternalData().getCompanyNumber(),
                    notificationId, requestBody.getExternalData().getData().getKind());

        } else {
            logger.info("PSC not persisted as the record provided is not the latest record.");
        }
    }

    private boolean isLatestRecord(String notificationId, OffsetDateTime deltaAt) {
        String formattedDate = deltaAt.format(dateTimeFormatter);
        List<PscDocument> pscDocuments = repository
                .findUpdatedPsc(notificationId, formattedDate);
        return pscDocuments.isEmpty();
    }

    /**
     * Save or update the mongo record.
     * @param contextId Chs kafka id.
     * @param notificationId Mongo id.
     * @param document  Transformed Data.
     */
    private void save(String contextId, String notificationId,
                      PscDocument document) {
        Created created = getCreatedFromCurrentRecord(notificationId);
        if (created == null) {
            document.setCreated(new Created().setAt(LocalDateTime.now()));
        } else {
            document.setCreated(created);
        }

        try {
            repository.save(document);
            logger.info(String.format(
                    "Company PSC record is updated in MongoDb for context id: %s and id: %s",
                    contextId, notificationId));
        } catch (IllegalArgumentException illegalArgumentEx) {
            throw new BadRequestException(illegalArgumentEx.getMessage());
        }
    }

    /**
     * Check whether we are updating a record and if so persist created time.
     * @param notificationId Mongo Id.
     * @return created if this is an update save the previous created to the new document.
     */
    private Created getCreatedFromCurrentRecord(String notificationId) {
        try {
            return repository.findById(notificationId).map(PscDocument::getCreated).orElse(null);
        } catch (Exception exception) {
            logger.error("exception thrown: " + exception.getMessage());
            return null;
        }
    }

    private PscDocument getPscDocument(String companyNumber, String notificationId)
            throws ResourceNotFoundException {
        Optional<PscDocument> pscDocument =
                repository.getPscByCompanyNumberAndId(companyNumber, notificationId);
        return pscDocument.orElseThrow(() ->
                new ResourceNotFoundException(HttpStatus.NOT_FOUND, String.format(
                        "Resource not found for company number: %s", companyNumber)));
    }

    /** Delete PSC record. */
    @Transactional
    public void deletePsc(String companyNumber,String notificationId)
            throws ResourceNotFoundException {
        PscDocument pscDocument = getPscDocument(companyNumber, notificationId);

        repository.delete(pscDocument);
        logger.info(String.format("PSC record with company number %s has been deleted",
                companyNumber));
    }

    /** Get PSC record. */
    /** and transform it into an individual PSC.*/
    public Individual getIndividualPsc(String companyNumber, String notificationId) {

        try {
            Optional<PscDocument> pscDocument =
                    repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                            .filter(document -> document.getData().getKind()
                                    .equals("individual-person-with-significant-control"));
            if (pscDocument.isEmpty()) {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "PSC document not found in Mongo with id " + notificationId);
            }
            Individual individual = transformer.transformPscDocToIndividual(pscDocument);
            if (individual == null) {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "Failed to transform PSCDocument to Individual");
            }
            return individual;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Unexpected error occurred while fetching PSC document");
        }
    }

    /** Get PSC record. */
    /** and transform it into an corportate entity PSC.*/
    public CorporateEntity getCorportateEntityPsc(String companyNumber, String notificationId) {

        try {
            Optional<PscDocument> pscDocument =
                    repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                            .filter(document -> document.getData().getKind()
                                    .equals("corporate-entity-person-with-significant-control")
                                    && document.getCompanyNumber().equals(companyNumber));
            if (pscDocument.isEmpty()) {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "Corporate Entity PSC document not found in Mongo with id "
                                + notificationId);
            }
            CorporateEntity corporateEntity = transformer.transformPscDocToCorporateEntity(pscDocument);
            if (corporateEntity == null) {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "Failed to transform PSCDocument to Corporate Entity");
            }
            return corporateEntity;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Unexpected error occurred while fetching PSC document");
        }
    }

}
