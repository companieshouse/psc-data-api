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
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
import uk.gov.companieshouse.api.model.psc.PscIndividualWithIdentityVerificationDetailsApi;
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
import uk.gov.companieshouse.pscdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.pscdataapi.exceptions.InternalDataException;
import uk.gov.companieshouse.pscdataapi.exceptions.NotFoundException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.models.Created;
import uk.gov.companieshouse.pscdataapi.models.Links;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDeleteRequest;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.repository.CompanyPscRepository;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;
import uk.gov.companieshouse.pscdataapi.transform.IdentityVerificationDetailsMapper;

@Component
public class CompanyPscService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    private static final String NOT_FOUND_MSG = "PSC document not found";
    private static final String INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL = "individual-person-with-significant-control";
    private static final String INDIVIDUAL_BENEFICIAL_OWNER = "individual-beneficial-owner";
    private static final String PUBLIC_REGISTER = "public-register";
    private static final String CORPORATE_ENTITY_PERSON_WITH_SIGNIFICANT_CONTROL = "corporate-entity-person-with-significant-control";
    private static final String CORPORATE_ENTITY_BENEFICIAL_OWNER = "corporate-entity-beneficial-owner";
    private static final String LEGAL_PERSON_PERSON_WITH_SIGNIFICANT_CONTROL = "legal-person-person-with-significant-control";
    private static final String LEGAL_PERSON_BENEFICIAL_OWNER = "legal-person-beneficial-owner";
    private static final String SUPER_SECURE_PERSON_WITH_SIGNIFICANT_CONTROL = "super-secure-person-with-significant-control";
    private static final String SUPER_SECURE_BENEFICIAL_OWNER = "super-secure-beneficial-owner";
    private static final String NO_INTERNAL_ID_MSG = "Sensitive data or internalId is null for notificationId: %s";

    private final FeatureFlags featureFlags;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");
    private final CompanyPscTransformer transformer;
    private final CompanyPscRepository repository;
    private final ChsKafkaApiService chsKafkaApiService;
    private final CompanyExemptionsApiService companyExemptionsApiService;
    private final CompanyMetricsApiService companyMetricsApiService;
    private final OracleQueryApiService oracleQueryApiService;
    private final IdentityVerificationDetailsMapper identityVerificationDetailsMapper;

    public CompanyPscService(final FeatureFlags featureFlags, final CompanyPscTransformer transformer, final CompanyPscRepository repository,
            final ChsKafkaApiService chsKafkaApiService, final CompanyExemptionsApiService companyExemptionsApiService,
            final CompanyMetricsApiService companyMetricsApiService, final OracleQueryApiService oracleQueryApiService,
            final IdentityVerificationDetailsMapper identityVerificationDetailsMapper) {
        this.featureFlags = featureFlags;
        this.transformer = transformer;
        this.repository = repository;
        this.chsKafkaApiService = chsKafkaApiService;
        this.companyExemptionsApiService = companyExemptionsApiService;
        this.companyMetricsApiService = companyMetricsApiService;
        this.oracleQueryApiService = oracleQueryApiService;
        this.identityVerificationDetailsMapper = identityVerificationDetailsMapper;
    }

    public void insertPscRecord(FullRecordCompanyPSCApi requestBody) {
        final String notificationId = requestBody.getExternalData().getNotificationId();
        boolean isLatestRecord = isLatestRecord(notificationId, requestBody.getInternalData().getDeltaAt());
        if (!isLatestRecord) {
            final String msg = "Received stale delta";
            LOGGER.error(msg, DataMapHolder.getLogMap());
            throw new ConflictException(msg);
        }

        PscDocument document = transformer.transformPscOnInsert(notificationId, requestBody);
        save(notificationId, document);
        chsKafkaApiService.invokeChsKafkaApi(requestBody.getExternalData().getCompanyNumber(), notificationId,
                requestBody.getExternalData().getData().getKind());
    }

    public void deletePsc(PscDeleteRequest deleteRequest) {
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
        PscIndividualFullRecordApi individualFullRecordApi = repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                .filter(document -> INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL.equals(document.getData().getKind()))
                .map(transformer::transformPscDocToIndividualFullRecord)
                .orElseThrow(() -> {
                    LOGGER.error(NOT_FOUND_MSG, DataMapHolder.getLogMap());
                    return new NotFoundException(NOT_FOUND_MSG);
                });

        if (featureFlags.isIndividualPscFullRecordAddidentityVerificationDetailsEnabled()) {
            oracleQueryApiService.getIdentityVerificationDetails(individualFullRecordApi.getInternalId())
                    .map(identityVerificationDetailsMapper::mapToIdentityVerificationDetails)
                    .ifPresent(individualFullRecordApi::setIdentityVerificationDetails);
        }

        return individualFullRecordApi;
    }

    public Individual getIndividualPsc(final String companyNumber, final String notificationId, final boolean registerView) {
        return repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                .filter(document -> INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL.equals(document.getData().getKind()))
                .map(document -> {
                    boolean showFullDateOfBirth = determineShowFullDob(companyNumber, registerView, document);
                    return transformer.transformPscDocToIndividual(document, showFullDateOfBirth);
                })
                .orElseThrow(() -> {
                    LOGGER.error(NOT_FOUND_MSG, DataMapHolder.getLogMap());
                    return new NotFoundException(NOT_FOUND_MSG);
                });
    }

    public PscIndividualWithIdentityVerificationDetailsApi getIndividualWithIdentityVerificationDetails(final String companyNumber,
            final String notificationId) {
        return repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                .filter(document -> INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL.equals(document.getData().getKind()))
                .map(document -> {
                    final PscIndividualWithIdentityVerificationDetailsApi individualWithIdentityVerificationDetails =
                            transformer.transformPscDocToIndividualWithIdentityVerificationDetails(document);

                    final Long internalId = Optional.ofNullable(document.getSensitiveData()).map(
                        PscSensitiveData::getInternalId).orElseThrow(() -> {
                        LOGGER.error(NO_INTERNAL_ID_MSG.formatted(notificationId), DataMapHolder.getLogMap());
                        return new InternalDataException(NO_INTERNAL_ID_MSG.formatted(notificationId));
                    });

                    oracleQueryApiService.getIdentityVerificationDetails(internalId).map(
                        identityVerificationDetailsMapper::mapToIdentityVerificationDetails).ifPresent(
                        individualWithIdentityVerificationDetails::setIdentityVerificationDetails);

                    return individualWithIdentityVerificationDetails;
                })
                .orElseThrow(() -> {
                    LOGGER.error(NOT_FOUND_MSG, DataMapHolder.getLogMap());
                    return new NotFoundException(NOT_FOUND_MSG);
                });
    }

    public IndividualBeneficialOwner getIndividualBeneficialOwnerPsc(final String companyNumber, final String notificationId,
            final boolean registerView) {
        return repository.findById(notificationId)
                .filter(document -> INDIVIDUAL_BENEFICIAL_OWNER.equals(document.getData().getKind())
                        && companyNumber.equals(document.getCompanyNumber()))
                .map(document -> {
                    boolean showFullDateOfBirth = determineShowFullDob(companyNumber, registerView, document);
                    return transformer.transformPscDocToIndividualBeneficialOwner(document, showFullDateOfBirth);
                })
                .orElseThrow(() -> {
                    LOGGER.error(NOT_FOUND_MSG, DataMapHolder.getLogMap());
                    return new NotFoundException(NOT_FOUND_MSG);
                });
    }

    public CorporateEntity getCorporateEntityPsc(final String companyNumber, final String notificationId) {
        return repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                .filter(document -> CORPORATE_ENTITY_PERSON_WITH_SIGNIFICANT_CONTROL.equals(document.getData().getKind())
                        && companyNumber.equals(document.getCompanyNumber()))
                .map(transformer::transformPscDocToCorporateEntity)
                .orElseThrow(() -> {
                    LOGGER.error(NOT_FOUND_MSG, DataMapHolder.getLogMap());
                    return new NotFoundException(NOT_FOUND_MSG);
                });
    }

    public CorporateEntityBeneficialOwner getCorporateEntityBeneficialOwnerPsc(final String companyNumber,
            final String notificationId) {
        return repository.findById(notificationId)
                .filter(document -> CORPORATE_ENTITY_BENEFICIAL_OWNER.equals(document.getData().getKind())
                        && companyNumber.equals(document.getCompanyNumber()))
                .map(transformer::transformPscDocToCorporateEntityBeneficialOwner)
                .orElseThrow(() -> {
                    LOGGER.error(NOT_FOUND_MSG, DataMapHolder.getLogMap());
                    return new NotFoundException(NOT_FOUND_MSG);
                });
    }

    public LegalPerson getLegalPersonPsc(final String companyNumber, final String notificationId) {
        return repository.findById(notificationId)
                .filter(document -> LEGAL_PERSON_PERSON_WITH_SIGNIFICANT_CONTROL.equals(document.getData().getKind())
                        && companyNumber.equals(document.getCompanyNumber()))
                .map(transformer::transformPscDocToLegalPerson)
                .orElseThrow(() -> {
                    LOGGER.error(NOT_FOUND_MSG, DataMapHolder.getLogMap());
                    return new NotFoundException(NOT_FOUND_MSG);
                });
    }

    public LegalPersonBeneficialOwner getLegalPersonBeneficialOwnerPsc(final String companyNumber, final String notificationId) {
        return repository.findById(notificationId)
                .filter(document -> LEGAL_PERSON_BENEFICIAL_OWNER.equals(document.getData().getKind())
                        && companyNumber.equals(document.getCompanyNumber()))
                .map(transformer::transformPscDocToLegalPersonBeneficialOwner)
                .orElseThrow(() -> {
                    LOGGER.error(NOT_FOUND_MSG, DataMapHolder.getLogMap());
                    return new NotFoundException(NOT_FOUND_MSG);
                });
    }

    public SuperSecure getSuperSecurePsc(final String companyNumber, final String notificationId) {
        return repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                .filter(document -> SUPER_SECURE_PERSON_WITH_SIGNIFICANT_CONTROL.equals(document.getData().getKind()))
                .map(transformer::transformPscDocToSuperSecure)
                .orElseThrow(() -> {
                    LOGGER.error(NOT_FOUND_MSG, DataMapHolder.getLogMap());
                    return new NotFoundException(NOT_FOUND_MSG);
                });
    }

    public SuperSecureBeneficialOwner getSuperSecureBeneficialOwnerPsc(final String companyNumber, final String notificationId) {
        return repository.getPscByCompanyNumberAndId(companyNumber, notificationId)
                .filter(document -> SUPER_SECURE_BENEFICIAL_OWNER.equals(document.getData().getKind()))
                .map(transformer::transformPscDocToSuperSecureBeneficialOwner)
                .orElseThrow(() -> {
                    LOGGER.error(NOT_FOUND_MSG, DataMapHolder.getLogMap());
                    return new NotFoundException(NOT_FOUND_MSG);
                });
    }

    public PscList retrievePscListSummaryFromDb(final String companyNumber, final int startIndex, final boolean registerView,
            final int itemsPerPage) {
        MetricsApi companyMetrics = companyMetricsApiService.getCompanyMetrics(companyNumber).orElseGet(() -> {
            LOGGER.info("No company metrics data found", DataMapHolder.getLogMap());
            return null;
        });

        if (registerView) {
            return retrievePscDocumentListFromDbRegisterView(companyMetrics, companyNumber, startIndex, itemsPerPage);
        }

        List<PscDocument> pscDocuments = repository.getPscDocumentList(companyNumber, startIndex, itemsPerPage);

        return createPscDocumentList(pscDocuments, startIndex, itemsPerPage, companyNumber, false, companyMetrics);
    }

    private boolean determineShowFullDob(final String companyNumber, final boolean registerView, PscDocument pscDocument) {
        if (!registerView) {
            return false;
        }

        MetricsApi metrics = companyMetricsApiService.getCompanyMetrics(companyNumber)
                .map(data -> {
                    if (data.getRegisters() == null) {
                        final String msg = "No company metrics registers data found";
                        LOGGER.error(msg, DataMapHolder.getLogMap());
                        throw new NotFoundException(msg);
                    }
                    return data;
                })
                .orElseThrow(() -> {
                    final String msg = "No company metrics data found";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    return new NotFoundException(msg);
                });

        RegisterApi pscRegister = metrics.getRegisters().getPersonsWithSignificantControl();
        if (!PUBLIC_REGISTER.equals(pscRegister.getRegisterMovedTo())) {
            LOGGER.info("Not on public register", DataMapHolder.getLogMap());
            return false;
        }

        final boolean isCeased = Optional.ofNullable(pscDocument.getData().getCeased())
                .orElseThrow(() -> {
                    final String msg = "No ceased status";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    return new NotFoundException(msg);
                });

        LocalDate movedToPublicRegister = Optional.ofNullable(pscRegister.getMovedOn())
                .map(OffsetDateTime::toLocalDate)
                .orElseThrow(() -> {
                    final String msg = "No moved on date";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    return new NotFoundException(msg);
                });

        LocalDate ceasedOn = Optional.ofNullable(pscDocument.getData().getCeasedOn())
                .orElseThrow(() -> {
                    final String msg = "No ceased on date";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    return new NotFoundException(msg);
                });

        return !isCeased || movedToPublicRegister.isBefore(ceasedOn);
    }

    private boolean isLatestRecord(final String notificationId, OffsetDateTime deltaAt) {
        String formattedDate = deltaAt.format(dateTimeFormatter);
        List<PscDocument> pscDocuments = repository
                .findUpdatedPsc(notificationId, formattedDate);
        return pscDocuments.isEmpty();
    }

    private void save(final String notificationId, PscDocument document) {
        Optional.ofNullable(getCreatedFromCurrentRecord(notificationId))
                .ifPresentOrElse(document::setCreated, () -> document.setCreated(new Created().setAt(LocalDateTime.now())));

        repository.save(document);
    }

    private Created getCreatedFromCurrentRecord(final String notificationId) {
        return repository.findById(notificationId)
                .map(PscDocument::getCreated)
                .orElse(null);
    }


    private PscList retrievePscDocumentListFromDbRegisterView(MetricsApi companyMetrics,
            String companyNumber, Integer startIndex, Integer itemsPerPage) {
        if (companyMetrics == null) {
            return createPscDocumentList(Collections.emptyList(), startIndex, itemsPerPage, companyNumber, true, null);
        }

        final String registerMovedTo = String.valueOf(Optional.of(companyMetrics)
                .map(MetricsApi::getRegisters)
                .map(RegistersApi::getPersonsWithSignificantControl)
                .map(RegisterApi::getRegisterMovedTo)
                .orElseThrow(() -> {
                    final String msg = "Company not on public register";
                    LOGGER.error(msg, DataMapHolder.getLogMap());
                    return new NotFoundException(msg);
                }));

        if (PUBLIC_REGISTER.equals(registerMovedTo)) {
            List<PscDocument> pscStatementDocuments = repository.getListSummaryRegisterView(companyNumber,
                    startIndex, companyMetrics.getRegisters().getPersonsWithSignificantControl().getMovedOn(),
                    itemsPerPage);

            return createPscDocumentList(pscStatementDocuments,
                    startIndex, itemsPerPage, companyNumber, true, companyMetrics);
        } else {
            final String msg = "Company not on public register";
            LOGGER.error(msg, DataMapHolder.getLogMap());
            throw new NotFoundException(msg);
        }
    }

    private PscList createPscDocumentList(List<PscDocument> pscDocuments, final int startIndex, final int itemsPerPage,
            final String companyNumber, final boolean registerView, MetricsApi companyMetrics) {
        PscList pscList = new PscList();

        List<PscData> pscData = pscDocuments.stream()
                .map(PscDocument::getData)
                .toList();
        List<ListSummary> documents = new ArrayList<>();

        for (PscDocument pscDocument : pscDocuments) {
            ListSummary listSummary = transformer.transformPscDocToListSummary(pscDocument, registerView);
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

        if (companyMetrics == null
                || companyMetrics.getCounts() == null // NOSONAR
                || companyMetrics.getCounts().getPersonsWithSignificantControl() == null) {
            LOGGER.info("No company metrics for PSC data found", DataMapHolder.getLogMap());
        } else {
            PscApi pscCounts = companyMetrics.getCounts().getPersonsWithSignificantControl();
            if (registerView) {
                final int withdrawnCount = (int) pscData.stream()
                        .filter(document -> document.getCeasedOn() != null)
                        .count();

                pscList.setCeasedCount(withdrawnCount);
                pscList.setTotalResults(pscCounts.getActivePscsCount() + pscList.getCeasedCount());
                pscList.setActiveCount(pscCounts.getActivePscsCount());
            } else {
                pscList.setActiveCount(pscCounts.getActivePscsCount());
                pscList.setCeasedCount(pscCounts.getCeasedPscsCount());
                pscList.setTotalResults(pscCounts.getPscsCount());
            }
        }

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

