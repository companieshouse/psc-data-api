package uk.gov.companieshouse.pscdataapi.service;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;
import static uk.gov.companieshouse.pscdataapi.util.DateUtils.isDeltaStale;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
import uk.gov.companieshouse.api.model.psc.PscIndividualWithVerificationStateApi;
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
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.config.FeatureFlags;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.pscdataapi.exceptions.ResourceNotFoundException;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.models.Created;
import uk.gov.companieshouse.pscdataapi.models.Links;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDeleteRequest;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.repository.CompanyPscRepository;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;
import uk.gov.companieshouse.pscdataapi.transform.VerificationStateMapper;

@Service
public class CompanyPscService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    private static final String NOT_ON_PUBLIC_REGISTER = "not-on-public-register";
    private static final String UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT =
            "Unexpected error occurred while fetching PSC document";
    private static final String NOT_FOUND_MSG = "PSC document not found";

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");
    private final FeatureFlags featureFlags;
    private final CompanyPscTransformer transformer;
    private final CompanyPscRepository repository;
    private final ChsKafkaApiService chsKafkaApiService;
    private final CompanyExemptionsApiService companyExemptionsApiService;
    private final CompanyMetricsApiService companyMetricsApiService;
    private final OracleQueryApiService oracleQueryApiService;
    private final VerificationStateMapper verificationStateMapper;

    public CompanyPscService(FeatureFlags featureFlags,
            CompanyPscTransformer transformer,
            CompanyPscRepository repository,
            ChsKafkaApiService chsKafkaApiService,
            CompanyExemptionsApiService companyExemptionsApiService,
            CompanyMetricsApiService companyMetricsApiService,
            OracleQueryApiService oracleQueryApiService,
            VerificationStateMapper verificationStateMapper) {
        this.featureFlags = featureFlags;
        this.transformer = transformer;
        this.repository = repository;
        this.chsKafkaApiService = chsKafkaApiService;
        this.companyExemptionsApiService = companyExemptionsApiService;
        this.companyMetricsApiService = companyMetricsApiService;
        this.oracleQueryApiService = oracleQueryApiService;
        this.verificationStateMapper = verificationStateMapper;
    }

    public void insertPscRecord(String contextId, FullRecordCompanyPSCApi requestBody)
            throws ServiceUnavailableException, BadRequestException {
        final String notificationId = requestBody.getExternalData().getNotificationId();
        boolean isLatestRecord = isLatestRecord(notificationId, requestBody.getInternalData().getDeltaAt());
        if (!isLatestRecord) {
            final String msg = "Received stale delta";
            LOGGER.error(msg, DataMapHolder.getLogMap());
            throw new ConflictException(msg);
        }

        PscDocument document = transformer.transformPscOnInsert(notificationId, requestBody);
        save(notificationId, document);
        chsKafkaApiService.invokeChsKafkaApi(contextId,
                requestBody.getExternalData().getCompanyNumber(),
                notificationId, requestBody.getExternalData().getData().getKind());
    }

    private boolean isLatestRecord(String notificationId, OffsetDateTime deltaAt) {
        String formattedDate = deltaAt.format(dateTimeFormatter);
        List<PscDocument> pscDocuments = repository
                .findUpdatedPsc(notificationId, formattedDate);
        return pscDocuments.isEmpty();
    }

    private void save(String notificationId, PscDocument document) {
        Created created = getCreatedFromCurrentRecord(notificationId);
        if (created == null) {
            document.setCreated(new Created().setAt(LocalDateTime.now()));
        } else {
            document.setCreated(created);
        }

        try {
            repository.save(document);
        } catch (IllegalArgumentException ex) {
            final String msg = "Invalid data provided";
            LOGGER.error(msg, ex, DataMapHolder.getLogMap());
            throw new BadRequestException(msg);
        }
    }

    private Created getCreatedFromCurrentRecord(String notificationId) {
        try {
            return repository.findById(notificationId).map(PscDocument::getCreated).orElse(null);
        } catch (Exception ex) {
            final String msg = "Error occurred while fetching created date";
            LOGGER.error(msg, ex, DataMapHolder.getLogMap());
            return null;
        }
    }

    public void deletePsc(PscDeleteRequest deleteRequest)
            throws ResourceNotFoundException, ServiceUnavailableException {
        Optional<PscDocument> pscDocument = repository.getPscByCompanyNumberAndId(deleteRequest.companyNumber(),
                deleteRequest.notificationId());
        PscDocument document = null;
        if (pscDocument.isPresent()) {
            document = pscDocument.get();
            deltaAtCheck(deleteRequest.deltaAt(), document);
            repository.delete(document);
            chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(deleteRequest, document);
        } else {
            final String msg = "PSC document not found during delete";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(deleteRequest, document);
        }
    }

    public PscIndividualFullRecordApi getIndividualFullRecord(final String companyNumber, final String notificationId) {
        try {
            final Optional<PscDocument> pscDocument = repository.getPscByCompanyNumberAndId(companyNumber,
                            notificationId)
                    .filter(document -> document.getData().getKind()
                            .equals("individual-person-with-significant-control"));

            if (pscDocument.isPresent()) {
                final PscIndividualFullRecordApi individualFullRecord = transformer.transformPscDocToIndividualFullRecord(
                        pscDocument.get());

                if (individualFullRecord == null) {
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                            "Failed to transform PSCDocument to Individual Full Record");
                }

                if (featureFlags.isIndividualPscFullRecordAddVerificationStateEnabled()) {
                    if (individualFullRecord.getInternalId() != null) {
                        oracleQueryApiService.getPscVerificationState(individualFullRecord.getInternalId())
                                .map(verificationStateMapper::mapToVerificationState)
                                .ifPresent(individualFullRecord::setVerificationState);
                    } else {
                        LOGGER.error("Internal ID not found in PSC document.", DataMapHolder.getLogMap());
                    }
                }
                return individualFullRecord;

            } else {
                final String msg = "Individual PSC document not found";
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } catch (ResourceNotFoundException ex) {
            LOGGER.error("Resource not found", ex, DataMapHolder.getLogMap());
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT, ex, DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT);
        }
    }

    public Individual getIndividualPsc(
            String companyNumber, String notificationId, Boolean registerView) {
        try {
            Optional<PscDocument> pscDocument =
                    repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                            .filter(document -> document.getData().getKind()
                                    .equals("individual-person-with-significant-control"));
            if (pscDocument.isPresent()) {
                boolean showFullDateOfBirth = determineShowFullDob(
                        companyNumber, registerView, pscDocument.get());

                Individual individual = transformer
                        .transformPscDocToIndividual(pscDocument.get(), showFullDateOfBirth);

                if (individual == null) {
                    final String msg = "Failed to transform PSC Document to Individual";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
                }
                return individual;
            } else {
                final String msg = "Individual PSC document not found";
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } catch (ResourceNotFoundException ex) {
            LOGGER.error("Resource not found", ex, DataMapHolder.getLogMap());
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT, ex, DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT);
        }
    }

    public PscIndividualWithVerificationStateApi getIndividualWithVerificationState(String companyNumber, String notificationId) {
        try {
            Optional<PscDocument> pscDocument = repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                    .filter(document -> document.getData().getKind()
                            .equals("individual-person-with-significant-control"));
            if (pscDocument.isPresent()) {
                Long internalId = pscDocument.get().getSensitiveData().getInternalId();

                PscIndividualWithVerificationStateApi individualWithVerificationState = transformer
                        .transformPscDocToIndividualWithVerificationState(pscDocument.get());

                if (individualWithVerificationState == null) {
                    final String msg = "Failed to transform PSCDocument to Psc Individual With Verification State";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
                }
                oracleQueryApiService.getPscVerificationState(internalId)
                        .map(verificationStateMapper::mapToVerificationState)
                        .ifPresent(individualWithVerificationState::setVerificationState);

                return individualWithVerificationState;
            } else {
                final String msg = NOT_FOUND_MSG;
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } catch (ResourceNotFoundException ex) {
            LOGGER.error("Resource not found", ex, DataMapHolder.getLogMap());
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT, ex, DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT);
        }
    }

    public IndividualBeneficialOwner getIndividualBeneficialOwnerPsc(
            String companyNumber, String notificationId, Boolean registerView) {
        try {
            Optional<PscDocument> pscDocument = repository.findById(notificationId)
                    .filter(document -> document.getData().getKind()
                            .equals("individual-beneficial-owner")
                            && document.getCompanyNumber().equals(companyNumber));
            if (pscDocument.isPresent()) {
                boolean showFullDateOfBirth = determineShowFullDob(
                        companyNumber, registerView, pscDocument.get());

                IndividualBeneficialOwner individualBeneficialOwner = transformer
                        .transformPscDocToIndividualBeneficialOwner(pscDocument.get(),
                                showFullDateOfBirth);

                if (individualBeneficialOwner == null) {
                    final String msg = "Failed to transform PSC Document to Individual Beneficial Owner";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
                }
                return individualBeneficialOwner;
            } else {
                final String msg = "Individual Beneficial Owner PSC document not found";
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } catch (ResourceNotFoundException ex) {
            LOGGER.error("Resource not found", ex, DataMapHolder.getLogMap());
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT, ex, DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT);
        }
    }

    private boolean determineShowFullDob(String companyNumber, boolean registerView,
            PscDocument pscDocument) throws ResourceNotFoundException {
        if (!registerView) {
            return false;
        } else {
            Optional<MetricsApi> companyMetrics = companyMetricsApiService.getCompanyMetrics(companyNumber);

            if (companyMetrics.isPresent()) {
                try {
                    String registerMovedTo = companyMetrics.get().getRegisters()
                            .getPersonsWithSignificantControl().getRegisterMovedTo();
                    if (registerMovedTo.equals("public-register")) {
                        Boolean isCeased = pscDocument.getData().getCeased();
                        boolean ceased = isCeased != null && isCeased;
                        LocalDate ceasedOn = pscDocument.getData().getCeasedOn();
                        LocalDate movedToPublicRegister = companyMetrics.get().getRegisters()
                                .getPersonsWithSignificantControl().getMovedOn().toLocalDate();

                        if (!ceased || movedToPublicRegister.isBefore(ceasedOn)) {
                            return true;
                        } else {
                            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                                    NOT_ON_PUBLIC_REGISTER);
                        }
                    } else {
                        throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                                NOT_ON_PUBLIC_REGISTER);
                    }
                } catch (ResourceNotFoundException ex) {
                    LOGGER.error("Resource not found", ex, DataMapHolder.getLogMap());
                    throw ex;
                } catch (Exception ex) {
                    LOGGER.error(NOT_ON_PUBLIC_REGISTER, ex, DataMapHolder.getLogMap());
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, NOT_ON_PUBLIC_REGISTER);
                }
            } else {
                final String msg = "No company metrics data found";
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        }
    }

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
                    final String msg = "Failed to transform PSC document to corporate entity";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
                }
                return corporateEntity;
            } else {
                final String msg = "Corporate Entity PSC document not found";
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT, ex, DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT);
        }
    }

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
                    final String msg = "Failed to transform PSC document to Corporate Entity Beneficial Owner";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
                }
                return corporateEntityBeneficialOwner;
            } else {
                final String msg = NOT_FOUND_MSG;
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT, ex, DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT);
        }
    }

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
                    final String msg = "Failed to transform PSC document to Legal Person";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
                }
                return legalPerson;
            } else {
                final String msg = NOT_FOUND_MSG;
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT, ex, DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT);
        }
    }

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
                    final String msg = "Failed to transform PSC document to Legal Person Beneficial Owner";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
                }
                return legalPersonBeneficialOwner;
            } else {
                final String msg = NOT_FOUND_MSG;
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT, ex, DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT);
        }
    }

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
                    final String msg = "Failed to transform PSC document to Super Secure";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
                }
                return superSecure;
            } else {
                final String msg = NOT_FOUND_MSG;
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT, ex, DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT);
        }
    }

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
                    final String msg = "Failed to transform PSC document to Super Secure Beneficial Owner";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
                }
                return superSecureBeneficialOwner;
            } else {
                final String msg = NOT_FOUND_MSG;
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT, ex, DataMapHolder.getLogMap());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, UNEXPECTED_ERROR_OCCURRED_WHILE_FETCHING_PSC_DOCUMENT);
        }
    }

    public PscList retrievePscListSummaryFromDb(String companyNumber, Integer startIndex,
            Boolean registerView, Integer itemsPerPage) {
        Optional<MetricsApi> companyMetrics = companyMetricsApiService.getCompanyMetrics(companyNumber);

        if (Boolean.TRUE.equals(registerView)) {
            return retrievePscDocumentListFromDbRegisterView(companyMetrics,
                    companyNumber, startIndex, itemsPerPage);
        }

        List<PscDocument> pscDocuments = repository.getPscDocumentList(companyNumber, startIndex, itemsPerPage);

        return createPscDocumentList(pscDocuments,
                startIndex, itemsPerPage, companyNumber, false, companyMetrics);
    }

    private PscList retrievePscDocumentListFromDbRegisterView(Optional<MetricsApi> companyMetrics,
            String companyNumber, Integer startIndex, Integer itemsPerPage) {
        MetricsApi metricsData;
        if (companyMetrics.isPresent()) {
            metricsData = companyMetrics.get();
            String registerMovedTo = String.valueOf(Optional.of(metricsData)
                    .map(MetricsApi::getRegisters)
                    .map(RegistersApi::getPersonsWithSignificantControl)
                    .map(RegisterApi::getRegisterMovedTo)
                    .orElseThrow(() -> {
                        final String msg = "Company not on public register";
                        LOGGER.error(msg, DataMapHolder.getLogMap());
                        return new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
                    }));

            if (registerMovedTo.equals("public-register")) {
                List<PscDocument> pscStatementDocuments = repository.getListSummaryRegisterView(companyNumber,
                        startIndex, metricsData.getRegisters().getPersonsWithSignificantControl().getMovedOn(),
                        itemsPerPage);

                return createPscDocumentList(pscStatementDocuments,
                        startIndex, itemsPerPage, companyNumber, true, companyMetrics);
            } else {
                final String msg = "Company not on public register";
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, msg);
            }
        } else {
            return createPscDocumentList(Collections.emptyList(),
                    startIndex, itemsPerPage, companyNumber, true, companyMetrics);
        }
    }

    private PscList createPscDocumentList(List<PscDocument> pscDocuments,
            Integer startIndex, Integer itemsPerPage,
            String companyNumber, boolean registerView,
            Optional<MetricsApi> companyMetrics) {
        PscList pscList = new PscList();

        List<PscData> pscData = pscDocuments.stream()
                .map(PscDocument::getData).toList();
        List<ListSummary> documents = new ArrayList<>();

        for (PscDocument pscDocument : pscDocuments) {
            ListSummary listSummary = this.transformer
                    .transformPscDocToListSummary(pscDocument, registerView);
            documents.add(listSummary);
        }

        if (pscDocuments.isEmpty()) {
            pscList.setActiveCount(0);
            pscList.setCeasedCount(0);
            pscList.setTotalResults(0);
        }

        Links links = new Links();
        links.setSelf(String.format("/company/%s/persons-with-significant-control", companyNumber));
        pscList.setItemsPerPage(itemsPerPage);
        pscList.setLinks(links);
        pscList.setStartIndex(startIndex);
        pscList.setItems(documents);

        if (hasActivePscExemptions(companyNumber)) {
            links.setExemptions(String.format("/company/%s/exemptions", companyNumber));
        }

        companyMetrics.ifPresentOrElse(metricsApi -> {
            try {
                if (registerView) {
                    long withdrawnCount = pscData.stream()
                            .filter(document -> document.getCeasedOn() != null).count();

                    pscList.setCeasedCount((int) withdrawnCount);
                    pscList.setTotalResults(metricsApi.getCounts()
                            .getPersonsWithSignificantControl().getActivePscsCount()
                            + pscList.getCeasedCount());
                    pscList.setActiveCount(metricsApi.getCounts()
                            .getPersonsWithSignificantControl().getActivePscsCount());
                } else {
                    pscList.setActiveCount(metricsApi.getCounts()
                            .getPersonsWithSignificantControl().getActivePscsCount());
                    pscList.setCeasedCount(metricsApi.getCounts()
                            .getPersonsWithSignificantControl().getCeasedPscsCount());
                    pscList.setTotalResults(metricsApi.getCounts()
                            .getPersonsWithSignificantControl().getPscsCount());
                }
            } catch (NullPointerException ex) {
                final String msg = "No PSC data in metrics";
                LOGGER.error(msg, ex, DataMapHolder.getLogMap());
            }
        }, () -> LOGGER.info("No company metrics counts data found", DataMapHolder.getLogMap()));

        return pscList;
    }

    private boolean hasActivePscExemptions(String companyNumber) {
        Optional<CompanyExemptions> companyExemptions = companyExemptionsApiService.getCompanyExemptions(companyNumber);

        return companyExemptions.filter(x ->
                x.getExemptions() != null &&
                        ((x.getExemptions().getPscExemptAsSharesAdmittedOnMarket() != null &&
                                x.getExemptions().getPscExemptAsSharesAdmittedOnMarket().getItems().stream()
                                        .anyMatch(e -> e.getExemptTo() == null)) ||
                                (x.getExemptions().getPscExemptAsTradingOnEuRegulatedMarket() != null &&
                                        x.getExemptions().getPscExemptAsTradingOnEuRegulatedMarket().getItems().stream()
                                                .anyMatch(e -> e.getExemptTo() == null)) ||
                                (x.getExemptions().getPscExemptAsTradingOnRegulatedMarket() != null &&
                                        x.getExemptions().getPscExemptAsTradingOnRegulatedMarket().getItems().stream()
                                                .anyMatch(e -> e.getExemptTo() == null)) ||
                                (x.getExemptions().getPscExemptAsTradingOnUkRegulatedMarket() != null &&
                                        x.getExemptions().getPscExemptAsTradingOnUkRegulatedMarket().getItems().stream()
                                                .anyMatch(e -> e.getExemptTo() == null)))).isPresent();
    }

    private void deltaAtCheck(String requestDeltaAt, PscDocument document) {
        if (isDeltaStale(requestDeltaAt, document.getDeltaAt())) {
            final String msg = "Stale delta received; request delta_at: [%s] is not after existing delta_at: [%s]".formatted(
                    requestDeltaAt, document.getDeltaAt());
            LOGGER.error(msg, DataMapHolder.getLogMap());
            throw new ConflictException(msg);
        }
    }
}
