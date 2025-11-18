package uk.gov.companieshouse.pscdataapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.pscdataapi.util.TestHelper.DELTA_AT;
import static uk.gov.companieshouse.pscdataapi.util.TestHelper.INDIVIDUAL_KIND;
import static uk.gov.companieshouse.pscdataapi.util.TestHelper.STALE_DELTA_AT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.exemptions.Exemptions;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnUkRegulatedMarketItem;
import uk.gov.companieshouse.api.metrics.CountsApi;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.PscApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Identification;
import uk.gov.companieshouse.api.psc.IdentityVerificationDetails;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.IndividualBeneficialOwner;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
import uk.gov.companieshouse.api.psc.LegalPerson;
import uk.gov.companieshouse.api.psc.LegalPersonBeneficialOwner;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.api.psc.SuperSecure;
import uk.gov.companieshouse.api.psc.SuperSecureBeneficialOwner;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.config.FeatureFlags;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.pscdataapi.exceptions.NotFoundException;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.models.Created;
import uk.gov.companieshouse.pscdataapi.models.Links;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDeleteRequest;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.repository.CompanyPscRepository;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

@ExtendWith(MockitoExtension.class)
class CompanyPscServiceTest {

    private static final String NOTIFICATION_ID = TestHelper.NOTIFICATION_ID;
    private static final String COMPANY_NUMBER = TestHelper.COMPANY_NUMBER;
    private static final Boolean REGISTER_VIEW_TRUE = true;
    private static final Boolean REGISTER_VIEW_FALSE = false;
    private static final boolean SHOW_FULL_DOB_TRUE = true;
    private static final boolean SHOW_FULL_DOB_FALSE = false;

    @InjectMocks
    private CompanyPscService service;

    @Captor
    private ArgumentCaptor<String> dateCaptor;

    @Mock
    private CompanyPscRepository repository;
    @Mock
    private CompanyPscTransformer transformer;
    @Mock
    private ChsKafkaApiService chsKafkaApiService;
    @Mock
    private CompanyExemptionsApiService companyExemptionsApiService;
    @Mock
    private CompanyMetricsApiService companyMetricsApiService;
    @Mock
    private FeatureFlags featureFlags;

    private FullRecordCompanyPSCApi request;
    private PscDocument pscDocument;
    private String dateString;
    private OffsetDateTime date;
    private OffsetDateTime laterDate;
    private TestHelper testHelper;
    private PscExemptAsTradingOnUkRegulatedMarketItem pscExemptAsTradingOnUkRegulatedMarketItem;

    @BeforeEach
    void setUp() {
        date = TestHelper.createOffsetDateTime();
        dateString = date.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS"));
        laterDate = TestHelper.createLaterOffsetDateTime();

        request = TestHelper.buildBasicFullRecordPsc();
        pscDocument = TestHelper.buildBasicDocument();

        testHelper = new TestHelper();
        pscExemptAsTradingOnUkRegulatedMarketItem = new PscExemptAsTradingOnUkRegulatedMarketItem();
    }

    @Test
    void insertBrandNewPscRecordSavesPsc() {
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        when(transformer.transformPscOnInsert(NOTIFICATION_ID, request)).thenReturn(pscDocument);

        service.insertPscRecord(request);

        verify(repository).save(pscDocument);
        verify(chsKafkaApiService).invokeChsKafkaApi(any(), any(), any());
        assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(pscDocument.getCreated().getAt());
    }

    @Test
    void insertUpdatePscRecordSavesPsc() {
        PscDocument oldRecord = new PscDocument();
        LocalDateTime localDateTime = LocalDateTime.now();
        oldRecord.setCreated(new Created().setAt(localDateTime));
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(oldRecord));
        when(transformer.transformPscOnInsert(NOTIFICATION_ID, request)).thenReturn(pscDocument);

        service.insertPscRecord(request);

        verify(repository).save(pscDocument);
        verify(chsKafkaApiService).invokeChsKafkaApi(any(), any(), any());
        assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(pscDocument.getCreated().getAt());
        assertEquals(localDateTime, pscDocument.getCreated().getAt());
    }

    @Test
    void retryOfFailedInsertUpdatePscRecordSavesPsc() {
        PscDocument oldRecord = new PscDocument();
        LocalDateTime localDateTime = LocalDateTime.now();
        oldRecord.setCreated(new Created().setAt(localDateTime));
        pscDocument.setCreated(new Created().setAt(localDateTime));

        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(List.of());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(oldRecord));
        when(transformer.transformPscOnInsert(NOTIFICATION_ID, request)).thenReturn(pscDocument);

        service.insertPscRecord(request);

        verify(repository).save(pscDocument);
        verify(chsKafkaApiService).invokeChsKafkaApi(any(), any(), any());
        assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(pscDocument.getCreated().getAt());
        assertEquals(localDateTime, pscDocument.getCreated().getAt());
    }

    @Test
    void insertStalePscRecordDoesNotSavePscWhenUpdateAlreadyMade() {

        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(
                List.of(new PscDocument()));

        Executable actual = () -> service.insertPscRecord(request);

        assertThrows(ConflictException.class, actual);
        verify(repository, never()).save(pscDocument);
        verify(chsKafkaApiService, never()).invokeChsKafkaApi(any(), any(), any());
        assertEquals(dateString, dateCaptor.getValue());
    }

    @Test
    @DisplayName("When company number & notification id is provided, delete PSC")
    void testDeletePSC() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(
                Optional.ofNullable(pscDocument));
        service.deletePsc(new PscDeleteRequest(COMPANY_NUMBER, NOTIFICATION_ID, "", INDIVIDUAL_KIND, DELTA_AT));

        verify(repository, times(1)).getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID);
        verify(repository, times(1)).delete(pscDocument);
        verify(chsKafkaApiService).invokeChsKafkaApiWithDeleteEvent(any(), any());
    }

    @Test
    @DisplayName("When company number is null throw Bad Request Exception")
    void testDeletePSCThrowsResourceBadRequestException() {
        when(repository.getPscByCompanyNumberAndId("", NOTIFICATION_ID)).thenThrow(BadRequestException.class);

        final var deleteRequest = new PscDeleteRequest("", NOTIFICATION_ID, "", INDIVIDUAL_KIND, DELTA_AT);

        assertThrows(BadRequestException.class, () -> service.deletePsc(deleteRequest));

        verify(repository, times(1)).getPscByCompanyNumberAndId("", NOTIFICATION_ID);
        verify(repository, never()).delete(any());
        verify(chsKafkaApiService, never()).invokeChsKafkaApiWithDeleteEvent(any(), any());
    }

    @Test
    @DisplayName("When company number and id is null throw BadRequestException")
    void testDeletePSCThrowsBadRequestExceptionWhenCompanyNumberAndNotificationIdIsNull() {
        when(repository.getPscByCompanyNumberAndId("", "")).thenThrow(BadRequestException.class);

        final var deleteRequest = new PscDeleteRequest("", "", "", INDIVIDUAL_KIND, DELTA_AT);

        assertThrows(BadRequestException.class, () -> service.deletePsc(deleteRequest));

        verify(repository, times(1)).getPscByCompanyNumberAndId("", "");
        verify(repository, never()).delete(any());
        verify(chsKafkaApiService, never()).invokeChsKafkaApiWithDeleteEvent(any(), any());
    }

    @Test
    @DisplayName("When Kafka notification fails throw ServiceUnavailableException")
    void testDeletePSCThrowsServiceUnavailableExceptionWhenKafkaNotification() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(
                Optional.of(pscDocument));
        when(chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(any(), any()))
                .thenThrow(new ServiceUnavailableException("message"));

        final var deleteRequest = new PscDeleteRequest(COMPANY_NUMBER, NOTIFICATION_ID, "", INDIVIDUAL_KIND, DELTA_AT);

        assertThrows(ServiceUnavailableException.class, () -> service.deletePsc(deleteRequest));

        verify(repository).getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID);
        verify(repository).delete(pscDocument);
        verify(chsKafkaApiService).invokeChsKafkaApiWithDeleteEvent(any(), any());
    }

    @Test
    @DisplayName("Kafka notification succeeds on retry and after document deleted")
    void testKafkaNotificationSucceedsOnRetryAfterDocumentDeleted() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.empty());

        service.deletePsc(new PscDeleteRequest(COMPANY_NUMBER, NOTIFICATION_ID, "", INDIVIDUAL_KIND, DELTA_AT));

        verify(repository).getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID);
        verify(chsKafkaApiService).invokeChsKafkaApiWithDeleteEvent(any(), any());

    }

    @Test
    void deleteIndividualFullRecordThrowsConflictWhenDeltaAtCheckFails() {
        PscDocument document = new PscDocument();
        document.setDeltaAt(DELTA_AT);
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.of(document));

        final var deleteRequest = new PscDeleteRequest(COMPANY_NUMBER, NOTIFICATION_ID, "", INDIVIDUAL_KIND,
                STALE_DELTA_AT);

        assertThrows(ConflictException.class, () -> service.deletePsc(deleteRequest));

        verify(repository).getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID);
        verify(chsKafkaApiService, never()).invokeChsKafkaApiWithDeleteEvent(any(), any());
    }

    @Test
    void getIndividualPscReturns404WhenRegisterViewIsTrueAndEmptyMetrics() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(new MetricsApi()));

        assertThrows(NotFoundException.class,
                () -> service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
    }

    @Test
    void getIndividualPscReturns404WhenRegisterViewIsTrueAndWrongRegisterMovedTo() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));
        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        registers.setPersonsWithSignificantControl(new RegisterApi().registerMovedTo("wrong"));
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        assertThrows(NotFoundException.class,
                () -> service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
    }

    @Test
    void getIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndNoMovedToDate() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));
        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        registers.setPersonsWithSignificantControl(new RegisterApi().registerMovedTo("public-register"));
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        assertThrows(NotFoundException.class,
                () -> service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
    }

    @Test
    void getIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateAndNoCeasedOn() {
        PscData pscData = pscDocument.getData();
        pscData.setCeased(true);
        pscDocument.setData(pscData);
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(date);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        assertThrows(NotFoundException.class,
                () -> service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
    }

    @Test
    void getIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateEqualsCeasedOn() {
        PscData pscData = pscDocument.getData();
        pscData.setCeased(true);
        pscData.setCeasedOn(date.toLocalDate());
        pscDocument.setData(pscData);
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(date);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        assertThrows(NotFoundException.class,
                () -> service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
    }

    @Test
    void getIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateAfterCeasedOn() {
        PscData pscData = pscDocument.getData();
        pscData.setCeased(true);
        pscData.setCeasedOn(date.toLocalDate());
        pscDocument.setData(pscData);
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(laterDate);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        assertThrows(NotFoundException.class,
                () -> service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
    }

    @Test
    void getIndividualPscReturns200WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateBeforeCeasedOn() {
        PscData pscData = pscDocument.getData();
        pscData.setCeased(true);
        pscData.setCeasedOn(laterDate.toLocalDate());
        pscDocument.setData(pscData);
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(date);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        Individual individual = new Individual();
        when(transformer.transformPscDocToIndividual(pscDocument, SHOW_FULL_DOB_TRUE)).thenReturn(individual);

        Individual result = service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE);
        assertEquals(individual, result);
    }

    @Test
    void getIndividualPscReturn200WhenRegisterViewIsFalse() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        Individual individual = new Individual();
        when(transformer.transformPscDocToIndividual(pscDocument, SHOW_FULL_DOB_FALSE)).thenReturn(individual);

        Individual result = service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_FALSE);
        assertEquals(individual, result);
    }

    @Test
    void getIndividualPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_FALSE));
    }

    @Test
    void getIndividualBeneficialOwnerPscReturns400WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateEqualsCeasedOn() {
        PscData pscData = pscDocument.getData();
        pscData.setCeased(true);
        pscData.setCeasedOn(date.toLocalDate());
        pscData.setKind(TestHelper.INDIVIDUAL_BO_KIND);
        pscDocument.setData(pscData);
        when(repository.findById(NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(date);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        assertThrows(NotFoundException.class, () ->
                service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
    }

    @Test
    void getIndividualBeneficialOwnerPscReturns200WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateBeforeCeasedOn() {
        PscData pscData = pscDocument.getData();
        pscData.setCeased(true);
        pscData.setCeasedOn(laterDate.toLocalDate());
        pscData.setKind(TestHelper.INDIVIDUAL_BO_KIND);
        pscDocument.setData(pscData);
        when(repository.findById(NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(date);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        IndividualBeneficialOwner individualBo = new IndividualBeneficialOwner();
        when(transformer.transformPscDocToIndividualBeneficialOwner(pscDocument, SHOW_FULL_DOB_TRUE)).thenReturn(
                individualBo);

        IndividualBeneficialOwner result = service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID,
                REGISTER_VIEW_TRUE);
        assertEquals(individualBo, result);
    }

    @Test
    void getIndividualBeneficialOwnerPscReturn200WhenRegisterViewIsFalse() {
        pscDocument.getData().setKind(TestHelper.INDIVIDUAL_BO_KIND);
        IndividualBeneficialOwner individualBeneficialOwner = new IndividualBeneficialOwner();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(pscDocument));
        when(transformer.transformPscDocToIndividualBeneficialOwner(pscDocument, SHOW_FULL_DOB_FALSE))
                .thenReturn(individualBeneficialOwner);

        IndividualBeneficialOwner result = service
                .getIndividualBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_FALSE);

        assertEquals(individualBeneficialOwner, result);
    }

    @Test
    void getIndividualBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_FALSE));
    }

    @Test
    void getWrongTypeIndividualBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_FALSE));
    }

    @Test
    void getCorporateEntityBeneficialOwnerPscReturn200() {
        pscDocument.getData().setKind("corporate-entity-beneficial-owner");
        CorporateEntityBeneficialOwner corporateEntityBeneficialOwner =
                new CorporateEntityBeneficialOwner();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(pscDocument));
        when(transformer.transformPscDocToCorporateEntityBeneficialOwner(pscDocument))
                .thenReturn(corporateEntityBeneficialOwner);

        CorporateEntityBeneficialOwner result = service
                .getCorporateEntityBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID);

        assertEquals(corporateEntityBeneficialOwner, result);
    }

    @Test
    void getCorporateEntityBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getCorporateEntityBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void getWrongTypeCorporateBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getCorporateEntityBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void getLegalPersonPscReturn200() {
        pscDocument.getData().setKind("legal-person-person-with-significant-control");
        LegalPerson legalPerson =
                new LegalPerson();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(pscDocument));
        when(transformer.transformPscDocToLegalPerson(pscDocument))
                .thenReturn(legalPerson);

        LegalPerson result = service
                .getLegalPersonPsc(COMPANY_NUMBER, NOTIFICATION_ID);

        assertEquals(legalPerson, result);
    }

    @Test
    void getLegalPersonPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getLegalPersonPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void getSuperSecurePscReturn200() {
        pscDocument.getData().setKind("super-secure-person-with-significant-control");
        SuperSecure superSecure = new SuperSecure();
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(
                Optional.of(pscDocument));
        when(transformer.transformPscDocToSuperSecure(pscDocument))
                .thenReturn(superSecure);

        SuperSecure result = service
                .getSuperSecurePsc(COMPANY_NUMBER, NOTIFICATION_ID);

        assertEquals(superSecure, result);
    }

    @Test
    void getSuperSecurePscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getSuperSecurePsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void getWrongTypeSuperSecurePscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getSuperSecurePsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void getSuperSecureBeneficialOwnerPscReturn200() {
        pscDocument.getData().setKind("super-secure-beneficial-owner");
        SuperSecureBeneficialOwner superSecureBeneficialOwner = new SuperSecureBeneficialOwner();
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(
                Optional.of(pscDocument));
        when(transformer.transformPscDocToSuperSecureBeneficialOwner(pscDocument))
                .thenReturn(superSecureBeneficialOwner);

        SuperSecureBeneficialOwner result = service
                .getSuperSecureBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID);

        assertEquals(superSecureBeneficialOwner, result);
    }

    @Test
    void getSuperSecureBeneficialOwnerPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getSuperSecureBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void getWrongTypeSuperSecureBeneficialOwnerPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getSuperSecureBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void getCorporateEntityPscReturn200() {
        pscDocument.getData().setKind("corporate-entity-person-with-significant-control");
        CorporateEntity corporateEntity = new CorporateEntity();
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(
                Optional.of(pscDocument));
        when(transformer.transformPscDocToCorporateEntity(pscDocument))
                .thenReturn(corporateEntity);

        CorporateEntity result = service.getCorporateEntityPsc(COMPANY_NUMBER, NOTIFICATION_ID);

        assertEquals(corporateEntity, result);
    }

    @Test
    void getCorporateEntityPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getCorporateEntityPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void getWrongTypeLegalPersonPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getLegalPersonPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void getLegalPersonBeneficialOwnerPscReturn200() {
        pscDocument.getData().setKind("legal-person-beneficial-owner");
        LegalPersonBeneficialOwner legalPersonBeneficialOwner =
                new LegalPersonBeneficialOwner();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(pscDocument));
        when(transformer.transformPscDocToLegalPersonBeneficialOwner(pscDocument))
                .thenReturn(legalPersonBeneficialOwner);

        LegalPersonBeneficialOwner result = service
                .getLegalPersonBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID);

        assertEquals(legalPersonBeneficialOwner, result);
    }

    @Test
    void getLegalPersonBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.getLegalPersonBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void getWrongTypeLegalPersonBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getLegalPersonBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void pscListReturnedByCompanyNumberFromRepository() throws NotFoundException {
        PscList expectedPscList = TestHelper.createPscList();
        PscData pscData = new PscData();
        pscDocument.setData(pscData);
        ListSummary listSummary = new ListSummary();
        Identification identification = new Identification();
        identification.setPlaceRegistered("x");
        identification.setCountryRegistered("x");
        identification.setRegistrationNumber("x");
        identification.setLegalAuthority("x");
        identification.setLegalForm("x");
        listSummary.setIdentification(identification);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.of(TestHelper.createMetrics()));
        when(repository.getPscDocumentList(anyString(), anyInt(), anyInt())).thenReturn(
                Collections.singletonList(pscDocument));
        when(transformer.transformPscDocToListSummary(pscDocument))
                .thenReturn(listSummary);

        PscList pscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, false, 25);

        assertEquals(expectedPscList, pscDocumentList);
        verify(repository, times(1)).getPscDocumentList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void pscListSummaryShouldNotShowDay() {
        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegister = new RegisterApi();
        pscRegister.registerMovedTo("public-register");
        registers.setPersonsWithSignificantControl(pscRegister);
        metrics.setRegisters(registers);
        metrics.setCounts(new CountsApi().personsWithSignificantControl(new PscApi().activePscsCount(1)));
        PscData pscData = new PscData();
        pscDocument = new PscDocument();
        pscDocument.setData(pscData);

        ListSummary listSummary = new ListSummary();
        uk.gov.companieshouse.api.psc.DateOfBirth dob = new uk.gov.companieshouse.api.psc.DateOfBirth();
        dob.setDay(null);
        dob.setMonth(5);
        dob.setYear(1980);
        listSummary.setDateOfBirth(dob);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));
        when(repository.getListSummaryRegisterView(any(), any(), any(), any())).thenReturn(
                Collections.singletonList(pscDocument));
        when(transformer.transformPscDocToListSummary(pscDocument)).thenReturn(listSummary);

        // when service method has registerView = true param set
        PscList result = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, true, 25);

        uk.gov.companieshouse.api.psc.DateOfBirth expectedDob = new uk.gov.companieshouse.api.psc.DateOfBirth();
        expectedDob.setDay(null);
        expectedDob.setMonth(5);
        expectedDob.setYear(1980);

        // then the DOB should have no day value in the response
        ListSummary returnedSummary = result.getItems().getFirst();
        assertEquals(expectedDob, returnedSummary.getDateOfBirth());
    }


    @Test
    void pscListWithNoMetricsReturnedByCompanyNumberFromRepository() throws NotFoundException {
        PscList expectedPscList = TestHelper.createPscListWithNoMetrics();
        PscData pscData = new PscData();
        pscDocument.setData(pscData);
        pscDocument.setId(TestHelper.PSC_ID);

        ListSummary listSummary = new ListSummary();
        Identification identification = new Identification();
        identification.setPlaceRegistered("x");
        identification.setCountryRegistered("x");
        identification.setRegistrationNumber("x");
        identification.setLegalAuthority("x");
        identification.setLegalForm("x");
        listSummary.setIdentification(identification);

        IdentityVerificationDetails identityVerificationDetails = new IdentityVerificationDetails();
        identityVerificationDetails.setAntiMoneyLaunderingSupervisoryBodies(Collections.singletonList("x"));
        identityVerificationDetails.setAppointmentVerificationEndOn(LocalDate.parse("2020-12-12"));
        identityVerificationDetails.setAppointmentVerificationStatementDate(LocalDate.parse("2020-10-10"));
        identityVerificationDetails.setAppointmentVerificationStatementDueOn(LocalDate.parse("2020-11-11"));
        identityVerificationDetails.setAppointmentVerificationStartOn(LocalDate.parse("2020-09-09"));
        identityVerificationDetails.setAuthorisedCorporateServiceProviderName("x");
        identityVerificationDetails.setIdentityVerifiedOn(LocalDate.parse("2020-11-11"));
        identityVerificationDetails.setPreferredName("x");
        listSummary.setIdentityVerificationDetails(identityVerificationDetails);

        when(repository.getPscDocumentList(anyString(), anyInt(), anyInt())).thenReturn(
                Collections.singletonList(pscDocument));

        when(transformer.transformPscDocToListSummary(pscDocument))
                .thenReturn(listSummary);

        PscList pscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, false, 25);

        IdentityVerificationDetails expectedIdentityVerificationDetails = new IdentityVerificationDetails();
        expectedIdentityVerificationDetails.setAntiMoneyLaunderingSupervisoryBodies(Collections.singletonList("x"));
        expectedIdentityVerificationDetails.setAppointmentVerificationEndOn(LocalDate.parse("2020-12-12"));
        expectedIdentityVerificationDetails.setAppointmentVerificationStatementDate(LocalDate.parse("2020-10-10"));
        expectedIdentityVerificationDetails.setAppointmentVerificationStatementDueOn(LocalDate.parse("2020-11-11"));
        expectedIdentityVerificationDetails.setAppointmentVerificationStartOn(LocalDate.parse("2020-09-09"));
        expectedIdentityVerificationDetails.setAuthorisedCorporateServiceProviderName("x");
        expectedIdentityVerificationDetails.setIdentityVerifiedOn(LocalDate.parse("2020-11-11"));
        expectedIdentityVerificationDetails.setPreferredName("x");

        expectedPscList.getItems().get(0).setIdentityVerificationDetails(expectedIdentityVerificationDetails);

        assertEquals(expectedPscList, pscDocumentList);
        verify(repository, times(1)).getPscDocumentList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void whenNoMetricsDataFoundForCompanyInRegisterViewShouldReturnEmptyList() throws NotFoundException {
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.empty());

        PscList expectedPscList = new PscList();
        Links links = new Links();
        links.setSelf("/company/companyNumber/persons-with-significant-control");
        links.setStatement(null);
        expectedPscList.setLinks(links);
        expectedPscList.setActiveCount(0);
        expectedPscList.setCeasedCount(0);
        expectedPscList.setTotalResults(0);
        expectedPscList.setItemsPerPage(25);
        expectedPscList.setStartIndex(0);

        PscList pscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, true, 25);
        assertEquals(expectedPscList, pscDocumentList);

    }

    @Test
    void whenCompanyNotInPublicRegisterGetPSCListShouldThrowNotFound() throws NotFoundException {
        MetricsApi metricsApi = TestHelper.createMetrics();
        RegistersApi registersApi = new RegistersApi();
        metricsApi.setRegisters(registersApi);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.of(metricsApi));

        Exception ex = assertThrows(NotFoundException.class,
                () -> service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, true, 25));

        String actualMessage = ex.getMessage();
        assertNotNull(actualMessage);
        verify(repository, times(0)).getListSummaryRegisterView(COMPANY_NUMBER, 0,
                OffsetDateTime.parse("2020-12-20T06:00Z"), 25);
    }

    @Test
    void pscListReturnedByCompanyNumberFromRepositoryWithExemptions() throws NotFoundException {
        PscList expectedPscList = TestHelper.createPscListWithExemptions();
        PscData pscData = new PscData();
        pscDocument.setData(pscData);
        ListSummary listSummary = new ListSummary();
        Identification identification = new Identification();
        identification.setPlaceRegistered("x");
        identification.setCountryRegistered("x");
        identification.setRegistrationNumber("x");
        identification.setLegalAuthority("x");
        identification.setLegalForm("x");
        listSummary.setIdentification(identification);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.of(TestHelper.createMetrics()));
        when(repository.getPscDocumentList(anyString(), anyInt(), anyInt())).thenReturn(
                Collections.singletonList(pscDocument));
        when(transformer.transformPscDocToListSummary(pscDocument))
                .thenReturn(listSummary);
        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(
                Optional.ofNullable(testHelper.createExemptions()));

        PscList pscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, false, 25);

        assertEquals(expectedPscList, pscDocumentList);
        verify(repository, times(1)).getPscDocumentList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void hasPscExemptionsReturnsTrueWhenTradingOnUkRegulatedMarket() {
        PscData pscData = new PscData();
        pscDocument.setData(pscData);
        ListSummary listSummary = new ListSummary();
        Identification identification = new Identification();
        identification.setPlaceRegistered("x");
        identification.setCountryRegistered("x");
        identification.setRegistrationNumber("x");
        identification.setLegalAuthority("x");
        identification.setLegalForm("x");
        listSummary.setIdentification(identification);
        when(repository.getPscDocumentList(anyString(), anyInt(), anyInt())).thenReturn(
                Collections.singletonList(pscDocument));
        when(transformer.transformPscDocToListSummary(pscDocument))
                .thenReturn(listSummary);

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(pscExemptAsTradingOnUkRegulatedMarketItem);
        companyExemptions.setExemptions(testHelper.getUkExemptions());
        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(
                Optional.ofNullable(testHelper.createExemptions()));

        PscList pscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, false, 25);

        Links links = new Links();
        links.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control");
        links.setExemptions("/company/" + COMPANY_NUMBER + "/exemptions");

        assertEquals(pscDocumentList.getLinks(), links);
    }

    @Test
    void getIndividualFullRecordShouldReturnFullRecordWhenFound_FlagVerificationDetailsFalse() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(
                Optional.of(pscDocument));
        when(transformer.transformPscDocToIndividualFullRecord(pscDocument)).thenReturn(new IndividualFullRecord());

        service.getIndividualFullRecord(COMPANY_NUMBER, NOTIFICATION_ID);

        verify(repository).getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID);
        verify(transformer).transformPscDocToIndividualFullRecord(pscDocument);
    }

    @Test
    void getIndividualFullRecordShouldReturnFullRecordWhenFound_FlagVerificationDetailsTrue() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(
                Optional.of(pscDocument));
        when(transformer.transformPscDocToIndividualFullRecord(pscDocument)).thenReturn(
                new IndividualFullRecord().internalId(123L));

        service.getIndividualFullRecord(COMPANY_NUMBER, NOTIFICATION_ID);

        verify(repository).getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID);
        verify(transformer).transformPscDocToIndividualFullRecord(pscDocument);
    }

    @Test
    void shouldSetFieldsWhenRegisterViewIsTrue() {
        // given
        MetricsApi metricsApi = new MetricsApi().counts(
                new CountsApi().personsWithSignificantControl(
                        new PscApi().activePscsCount(1))).registers(
                new RegistersApi().personsWithSignificantControl(
                        new RegisterApi().registerMovedTo("public-register")));

        Links links = new Links();
        links.setSelf("/company/%s/persons-with-significant-control".formatted(COMPANY_NUMBER));

        final PscList expected = new PscList()
                .itemsPerPage(25)
                .links(links)
                .startIndex(0)
                .items(List.of(new ListSummary()))
                .ceasedCount(0)
                .totalResults(1)
                .activeCount(1);

        when(companyMetricsApiService.getCompanyMetrics(anyString())).thenReturn(Optional.of(metricsApi));
        when(repository.getListSummaryRegisterView(any(), any(), any(), any())).thenReturn(
                Collections.singletonList(pscDocument));
        when(transformer.transformPscDocToListSummary(any())).thenReturn(new ListSummary());

        // when
        final PscList actual = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, true, 25);

        // then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("nullCompanyMetricsPscDataArgs")
    void shouldTestIfCompanyMetricsPscDataIsNull(MetricsApi metricsApi) {
        // given
        Links links = new Links();
        links.setSelf("/company/%s/persons-with-significant-control".formatted(COMPANY_NUMBER));

        final PscList expected = new PscList()
                .itemsPerPage(25)
                .links(links)
                .startIndex(0)
                .items(List.of(new ListSummary()));

        when(companyMetricsApiService.getCompanyMetrics(anyString())).thenReturn(Optional.of(metricsApi));
        when(repository.getListSummaryRegisterView(any(), any(), any(), any())).thenReturn(
                Collections.singletonList(pscDocument));
        when(transformer.transformPscDocToListSummary(any())).thenReturn(new ListSummary());

        // when
        final PscList actual = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, true, 25);

        // then
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> nullCompanyMetricsPscDataArgs() {
        return Stream.of(
                Arguments.of(Named.of("Metrics with counts but no psc data",
                        new MetricsApi().counts(new CountsApi()).registers(
                                new RegistersApi().personsWithSignificantControl(
                                        new RegisterApi().registerMovedTo("public-register"))))),
                Arguments.of(Named.of("Metrics without counts",
                        new MetricsApi().registers(
                                new RegistersApi().personsWithSignificantControl(
                                        new RegisterApi().registerMovedTo("public-register")))))
        );
    }
}
