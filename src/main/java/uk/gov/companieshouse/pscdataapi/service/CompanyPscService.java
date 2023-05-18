package uk.gov.companieshouse.pscdataapi.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
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
    public void insertPscRecord(String contextId, FullRecordCompanyPSCApi requestBody) {
        String notificationId = requestBody.getExternalData().getNotificationId();
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
        Created created = new Created();
        try {
            return repository.findById(notificationId).map(PscDocument::getCreated).orElse(null);
        } catch (Exception exception) {
            logger.error("exception thrown: " + exception.getMessage());
        }
        return created;
    }

}
