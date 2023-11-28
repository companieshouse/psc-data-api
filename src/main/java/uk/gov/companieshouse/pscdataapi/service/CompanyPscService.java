package uk.gov.companieshouse.pscdataapi.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.api.CompanyMetricsApiService;
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.IndividualBeneficialOwner;
import uk.gov.companieshouse.api.psc.LegalPerson;
import uk.gov.companieshouse.api.psc.LegalPersonBeneficialOwner;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.api.psc.SuperSecure;
import uk.gov.companieshouse.api.psc.SuperSecureBeneficialOwner;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.models.Created;
import uk.gov.companieshouse.pscdataapi.models.Links;
import uk.gov.companieshouse.pscdataapi.models.PscData;
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
    @Autowired
    CompanyMetricsApiService companyMetricsApiService;

    /**
     * Save or update a PSC.
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
            logger.info("PSC not persisted as the record provided is not the latest record.",
                    DataMapHolder.getLogMap());
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
     * @param contextId Chs kafka Id.
     * @param notificationId Mongo Id.
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
                    contextId, notificationId), DataMapHolder.getLogMap());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
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
        } catch (Exception ex) {
            logger.error(ex.getMessage(), DataMapHolder.getLogMap());
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

    /**
     * Delete PSC record.
     * @param companyNumber Company number.
     * @param notificationId Mongo Id.
     */
    @Transactional
    public void deletePsc(String companyNumber,String notificationId)
            throws ResourceNotFoundException {
        PscDocument pscDocument = getPscDocument(companyNumber, notificationId);
        repository.delete(pscDocument);
        logger.info(String.format("PSC record with company number %s has been deleted",
                companyNumber), DataMapHolder.getLogMap());
    }

    /** Get PSC record and transform it into an Individual PSC.
     * @param companyNumber Company number.
     * @param notificationId Mongo Id.
     * @param registerView Register View permission.
     * @return Individual PSC object.
     */
    public Individual getIndividualPsc(
            String companyNumber, String notificationId, Boolean registerView) {
        try {
            Optional<PscDocument> pscDocument =
                    repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                            .filter(document -> document.getData().getKind()
                                    .equals("individual-person-with-significant-control"));
            if (pscDocument.isPresent()) {
                Individual individual = transformer
                        .transformPscDocToIndividual(pscDocument.get(), registerView);
                if (individual == null) {
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                            "Failed to transform PSCDocument to Individual");
                }
                return individual;
            } else {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "Individual PSC document not found in Mongo with id " + notificationId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Unexpected error occurred while fetching PSC document");
        }
    }

    /** Get PSC record and transform it into an Individual Beneficial Owner PSC.
     * @param companyNumber Company number.
     * @param notificationId Mongo Id.
     * @param registerView Register View permission.
     * @return Individual Beneficial Owner PSC object.
     */
    public IndividualBeneficialOwner getIndividualBeneficialOwnerPsc(
            String companyNumber, String notificationId,Boolean registerView) {
        try {
            Optional<PscDocument> pscDocument = repository.findById(notificationId)
                    .filter(document -> document.getData().getKind()
                            .equals("individual-beneficial-owner")
                            && document.getCompanyNumber().equals(companyNumber));
            if (pscDocument.isPresent()) {
                IndividualBeneficialOwner individualBeneficialOwner = transformer
                        .transformPscDocToIndividualBeneficialOwner(pscDocument.get(),registerView);
                if (individualBeneficialOwner == null) {
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                            "Failed to transform PSCDocument to IndividualBeneficialOwner");
                }
                return individualBeneficialOwner;
            } else {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "Individual Beneficial Owner PSC document not found in Mongo with id"
                                + notificationId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Unexpected error occurred while fetching PSC document");
        }
    }

    /** Get PSC record and transform it into a Corporate Entity PSC.
     * @param companyNumber Company number.
     * @param notificationId Mongo Id.
     * @return Corporate Entity PSC object.
     */
    public CorporateEntity getCorporateEntityPsc(String companyNumber, String notificationId) {
        try {
            Optional<PscDocument> pscDocument =
                    repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                            .filter(document -> document.getData().getKind()
                                    .equals("corporate-entity-person-with-significant-control")
                                    && document.getCompanyNumber().equals(companyNumber));
            if (pscDocument.isPresent()) {
                CorporateEntity corporateEntity =
                        transformer.transformPscDocToCorporateEntity(pscDocument.get());
                if (corporateEntity == null) {
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                            "Failed to transform PSCDocument to Corporate Entity");
                }
                return corporateEntity;
            } else {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "Corporate Entity PSC document not found in Mongo with id "
                                + notificationId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Unexpected error occurred while fetching PSC document");
        }
    }

    /** Get PSC record and transform it into a Corporate Entity Beneficial Owner PSC.
     * @param companyNumber Company number.
     * @param notificationId Mongo Id.
     * @return Corporate Entity Beneficial Owner PSC object.
     */
    public CorporateEntityBeneficialOwner getCorporateEntityBeneficialOwnerPsc(
            String companyNumber, String notificationId) {
        try {
            Optional<PscDocument> pscDocument = repository.findById(notificationId)
                            .filter(document -> document.getData().getKind()
                                    .equals("corporate-entity-beneficial-owner")
                                    && document.getCompanyNumber().equals(companyNumber));
            if (pscDocument.isPresent()) {
                CorporateEntityBeneficialOwner corporateEntityBeneficialOwner =
                        transformer.transformPscDocToCorporateEntityBeneficialOwner(
                                pscDocument.get());
                if (corporateEntityBeneficialOwner == null) {
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                            "Failed to transform PSCDocument to "
                                    + "CorporateEntityBeneficialOwnerOwner");
                }
                return corporateEntityBeneficialOwner;
            } else {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "Corporate Entity Beneficial Owner PSC document not found in Mongo with id"
                                + notificationId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Unexpected error occurred while fetching PSC document");
        }
    }

    /** Get PSC record and transform it into a Legal Person PSC.
     * @param companyNumber Company number.
     * @param notificationId Mongo Id.
     * @return Legal Person PSC object.
     */
    public LegalPerson getLegalPersonPsc(String companyNumber, String notificationId) {
        try {
            Optional<PscDocument> pscDocument = repository.findById(notificationId)
                            .filter(document -> document.getData().getKind()
                                    .equals("legal-person-person-with-significant-control")
                                    && document.getCompanyNumber().equals(companyNumber));
            if (pscDocument.isPresent()) {
                LegalPerson legalPerson =
                        transformer.transformPscDocToLegalPerson(pscDocument.get());
                if (legalPerson == null) {
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                            "Failed to transform PSCDocument to Legal Person");
                }
                return legalPerson;
            } else {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "Legal person PSC document not found in Mongo with id" + notificationId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Unexpected error occurred while fetching PSC document");
        }
    }

    /** Get PSC record and transform it into a Legal Person Beneficial Owner PSC.
     * @param companyNumber Company number.
     * @param notificationId Mongo Id.
     * @return Legal Person Beneficial Owner PSC object.
     */
    public LegalPersonBeneficialOwner getLegalPersonBeneficialOwnerPsc(
            String companyNumber, String notificationId) {
        try {
            Optional<PscDocument> pscDocument = repository.findById(notificationId)
                            .filter(document -> document.getData().getKind()
                                    .equals("legal-person-beneficial-owner")
                                    && document.getCompanyNumber().equals(companyNumber));
            if (pscDocument.isPresent()) {
                LegalPersonBeneficialOwner legalPersonBeneficialOwner =
                        transformer.transformPscDocToLegalPersonBeneficialOwner(pscDocument.get());
                if (legalPersonBeneficialOwner == null) {
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                            "Failed to transform PSCDocument to Legal Person Beneficial Owner");
                }
                return legalPersonBeneficialOwner;
            } else {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "Legal person beneficial owner PSC document not found in Mongo with id"
                                + notificationId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Unexpected error occurred while fetching PSC document");
        }
    }

    /** Get PSC record and transform it into a Super Secure PSC.
     * @param companyNumber Company number.
     * @param notificationId Mongo Id.
     * @return Super Secure PSC object.
     */
    public SuperSecure getSuperSecurePsc(String companyNumber, String notificationId) {
        try {
            Optional<PscDocument> pscDocument =
                    repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                            .filter(document -> document.getData().getKind()
                                    .equals("super-secure-person-with-significant-control"));
            if (pscDocument.isPresent()) {
                SuperSecure superSecure = transformer
                        .transformPscDocToSuperSecure(pscDocument.get());
                if (superSecure == null) {
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                            "Failed to transform PSCDocument to SuperSecure");
                }
                return superSecure;
            } else {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "SuperSecure PSC document not found in Mongo with id " + notificationId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Unexpected error occurred while fetching PSC document");
        }
    }

    /** Get PSC record and transform it into a Super Secure Beneficial Owner PSC.
     * @param companyNumber Company number.
     * @param notificationId Mongo Id.
     * @return Super Secure Beneficial Owner PSC object.
     */
    public SuperSecureBeneficialOwner getSuperSecureBeneficialOwnerPsc(
            String companyNumber, String notificationId) {
        try {
            Optional<PscDocument> pscDocument =
                    repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                            .filter(document -> document.getData().getKind()
                                    .equals("super-secure-beneficial-owner"));
            if (pscDocument.isPresent()) {
                SuperSecureBeneficialOwner superSecureBeneficialOwner =
                        transformer.transformPscDocToSuperSecureBeneficialOwner(pscDocument.get());
                if (superSecureBeneficialOwner == null) {
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                            "Failed to transform PSCDocument to SuperSecureBeneficialOwner");
                }
                return superSecureBeneficialOwner;
            } else {
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        "SuperSecureBeneficialOwner PSC document not found in Mongo with id "
                                + notificationId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Unexpected error occurred while fetching PSC document");
        }
    }

    /** Get PSC List from database.
     * @param companyNumber Company number.
     * @param startIndex Start index.
     * @param registerView Register View permission.
     * @param itemsPerPage Items per page.
     * @return PscList object.
     */
    public PscList retrievePscListSummaryFromDb(String companyNumber, Integer startIndex,
                                                boolean registerView, Integer itemsPerPage) {

        Optional<MetricsApi> companyMetrics =
                companyMetricsApiService.getCompanyMetrics(companyNumber);

        if (registerView) {
            return retrievePscDocumentListFromDbRegisterView(companyMetrics,
                    companyNumber, startIndex, itemsPerPage);
        }

        Optional<List<PscDocument>> pscDocumentListOptional = repository
                .getPscDocumentList(companyNumber, startIndex, itemsPerPage);
        List<PscDocument> pscDocuments = pscDocumentListOptional
                .filter(docs -> !docs.isEmpty()).orElseThrow(() ->
                        new ResourceNotFoundException(HttpStatus.NOT_FOUND, String.format(
                                "Resource not found for company number: %s", companyNumber)));

        return createPscDocumentList(pscDocuments,
                startIndex, itemsPerPage, companyNumber, false, companyMetrics);
    }

    private PscList retrievePscDocumentListFromDbRegisterView(Optional<MetricsApi> companyMetrics,
            String companyNumber, Integer startIndex, Integer itemsPerPage) {

        logger.info(String.format("In register view for company number: %s", companyNumber),
                DataMapHolder.getLogMap());
        MetricsApi metricsData;
        if (companyMetrics.isPresent()) {
            metricsData = companyMetrics.get();
        } else {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    String.format("No company metrics data found for company number: %s",
                            companyNumber));
        }
        String registerMovedTo = Optional.of(metricsData)
                .map(MetricsApi::getRegisters)
                .map(RegistersApi::getPersonsWithSignificantControl)
                .map(RegisterApi::getRegisterMovedTo)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                        String.format("company %s not on public register", companyNumber)));


        if (registerMovedTo.equals("public-register")) {
            Optional<List<PscDocument>> pscListOptional = repository
                    .getListSummaryRegisterView(companyNumber, startIndex,
                            metricsData.getRegisters().getPersonsWithSignificantControl()
                                    .getMovedOn(),
                            itemsPerPage);
            List<PscDocument> pscStatementDocuments = pscListOptional
                    .filter(docs -> !docs.isEmpty()).orElseThrow(() ->
                            new ResourceNotFoundException(HttpStatus.NOT_FOUND, String.format(
                                    "Resource not found for company number: %s", companyNumber)));

            return createPscDocumentList(pscStatementDocuments,
                    startIndex, itemsPerPage, companyNumber, true, companyMetrics);
        } else {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    String.format("company %s not on public register", companyNumber));

        }
    }

    private PscList createPscDocumentList(List<PscDocument> pscDocuments,
                                          Integer startIndex, Integer itemsPerPage,
                                          String companyNumber, boolean registerView,
                                          Optional<MetricsApi> companyMetrics) {
        PscList pscList = new PscList();

        List<PscData> pscData = pscDocuments.stream()
                .map(PscDocument::getData).collect(Collectors.toList());
        List<ListSummary> documents = new ArrayList<>();

        for (PscDocument pscDocument : pscDocuments) {
            ListSummary listSummary = this.transformer.transformPscDocToListSummary(pscDocument);
            documents.add(listSummary);
        }

        Links links = new Links();
        links.setSelf(String.format("/company/%s/persons-with-significant-control", companyNumber));
        pscList.setItemsPerPage(itemsPerPage);
        pscList.setLinks(links);
        pscList.setStartIndex(startIndex);
        pscList.setItems(documents);

        companyMetrics.ifPresentOrElse(metricsApi -> {
            try {
                if (registerView) {
                    long withdrawnCount = pscData.stream()
                            .filter(document -> document.getCeasedOn() != null).count();

                    pscList.setCeasedCount((int) withdrawnCount);
                    pscList.setTotalResults(metricsApi.getCounts()
                            .getPersonsWithSignificantControl().getActiveStatementsCount()
                            + pscList.getCeasedCount());
                    pscList.setActiveCount(metricsApi.getCounts()
                            .getPersonsWithSignificantControl().getActiveStatementsCount());
                } else {
                    pscList.setActiveCount(metricsApi.getCounts()
                            .getPersonsWithSignificantControl().getActiveStatementsCount());
                    pscList.setCeasedCount(metricsApi.getCounts()
                            .getPersonsWithSignificantControl().getWithdrawnStatementsCount());
                    pscList.setTotalResults(metricsApi.getCounts()
                            .getPersonsWithSignificantControl().getStatementsCount());
                }
            } catch (NullPointerException exp) {
                logger.error(String.format("No PSC data in metrics for company number %s",
                        companyNumber), DataMapHolder.getLogMap());
            }
        }, () -> logger.info(String.format(
                "No company metrics counts data found for company number: %s",
                companyNumber), DataMapHolder.getLogMap()));

        return pscList;
    }

}
