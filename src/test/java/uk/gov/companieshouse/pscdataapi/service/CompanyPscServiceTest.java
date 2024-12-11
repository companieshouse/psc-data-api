package uk.gov.companieshouse.pscdataapi.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.api.api.CompanyMetricsApiService;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.exemptions.Exemptions;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnUkRegulatedMarketItem;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Identification;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.IndividualBeneficialOwner;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
import uk.gov.companieshouse.api.psc.LegalPerson;
import uk.gov.companieshouse.api.psc.LegalPersonBeneficialOwner;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.api.psc.SuperSecure;
import uk.gov.companieshouse.api.psc.SuperSecureBeneficialOwner;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.pscdataapi.exceptions.ResourceNotFoundException;
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

    @Mock
    private Logger logger;
    @Mock
    private CompanyPscRepository repository;
    @Mock
    private CompanyPscTransformer transformer;
    @Mock
    private ChsKafkaApiService chsKafkaApiService;
    @Mock
    CompanyExemptionsApiService companyExemptionsApiService;
    @Captor
    private ArgumentCaptor<String> dateCaptor;
    @InjectMocks
    private CompanyPscService service;
    @Mock
    CompanyMetricsApiService companyMetricsApiService;

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

        service.insertPscRecord("", request);

        verify(repository).save(pscDocument);
        verify(chsKafkaApiService).invokeChsKafkaApi(any(), any(), any(), any());
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

        service.insertPscRecord("", request);

        verify(repository).save(pscDocument);
        verify(chsKafkaApiService).invokeChsKafkaApi(any(), any(), any(), any());
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

        service.insertPscRecord("", request);

        verify(repository).save(pscDocument);
        verify(chsKafkaApiService).invokeChsKafkaApi(any(), any(), any(), any());
        assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(pscDocument.getCreated().getAt());
        assertEquals(localDateTime, pscDocument.getCreated().getAt());
    }

    @Test
    void insertStalePscRecordDoesNotSavePscWhenUpdateAlreadyMade() {

        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(List.of(new PscDocument()));

        service.insertPscRecord("", request);

        verify(repository, never()).save(pscDocument);
        verify(chsKafkaApiService, never()).invokeChsKafkaApi(any(), any(), any(), any());
        assertEquals(dateString, dateCaptor.getValue());
    }

    @Test
    void throwsBadRequestExceptionWhenNotGivenDocument() {
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), any())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        when(transformer.transformPscOnInsert(NOTIFICATION_ID, request)).thenReturn(pscDocument);
        when(repository.save(pscDocument)).thenThrow(new IllegalArgumentException());

        assertThrows(BadRequestException.class, () -> service.insertPscRecord("", request));
    }

    @Test
    void insertNewCreatedWhenCreatedCallToMongoFails() {
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), any())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenThrow(new RuntimeException());
        when(transformer.transformPscOnInsert(NOTIFICATION_ID, request)).thenReturn(pscDocument);

        service.insertPscRecord("", request);

        verify(repository).save(pscDocument);
        verify(chsKafkaApiService).invokeChsKafkaApi(any(), any(), any(), any());
        assertNotNull(pscDocument.getCreated().getAt());
    }

    @Test
    @DisplayName("When company number & notification id is provided, delete PSC")
    void testDeletePSC() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.ofNullable(pscDocument));
        service.deletePsc(new PscDeleteRequest(COMPANY_NUMBER, NOTIFICATION_ID, "", INDIVIDUAL_KIND, DELTA_AT));

        verify(repository, times(1)).getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID);
        verify(repository, times(1)).delete(pscDocument);
        verify(chsKafkaApiService).invokeChsKafkaApiWithDeleteEvent(any(), any());
    }

    @Test
    @DisplayName("When company number is null throw Bad Request Exception")
    void testDeletePSCThrowsResourceNotFoundException() {
        when(repository.getPscByCompanyNumberAndId("", NOTIFICATION_ID)).thenThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> service.deletePsc(new PscDeleteRequest("", NOTIFICATION_ID, "", INDIVIDUAL_KIND, DELTA_AT)));

        verify(repository, times(1)).getPscByCompanyNumberAndId("", NOTIFICATION_ID);
        verify(repository, never()).delete(any());
        verify(chsKafkaApiService, never()).invokeChsKafkaApiWithDeleteEvent(any(), any());
    }

    @Test
    @DisplayName("When company number and id is null throw ResourceNotFound Exception")
    void testDeletePSCThrowsNotFoundExceptionWhenCompanyNumberAndNotificationIdIsNull() {
        when(repository.getPscByCompanyNumberAndId("", "")).thenThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> service.deletePsc(new PscDeleteRequest("", "", "", INDIVIDUAL_KIND, DELTA_AT)));

        verify(repository, times(1)).getPscByCompanyNumberAndId("", "");
        verify(repository, never()).delete(any());
        verify(chsKafkaApiService, never()).invokeChsKafkaApiWithDeleteEvent(any(), any());
    }

    @Test
    @DisplayName("When Kafka notification fails throw ServiceUnavailableException")
    void testDeletePSCThrowsServiceUnavailableExceptionWhenKafkaNotification() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.of(pscDocument));
        when(chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(any(), any()))
                .thenThrow(new ServiceUnavailableException("message"));

        assertThrows(ServiceUnavailableException.class, () -> service.deletePsc(new PscDeleteRequest(COMPANY_NUMBER, NOTIFICATION_ID, "", INDIVIDUAL_KIND, DELTA_AT)));

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

        assertThrows(ConflictException.class, () -> service.deletePsc(new PscDeleteRequest(COMPANY_NUMBER, NOTIFICATION_ID, "", INDIVIDUAL_KIND, STALE_DELTA_AT)));

        verify(repository).getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID);
        verify(chsKafkaApiService, never()).invokeChsKafkaApiWithDeleteEvent(any(), any());
    }


    @Test
    void GetIndividualPscReturns404WhenRegisterViewIsTrueAndNoMetrics() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"No company metrics data found for company number: " + COMPANY_NUMBER + "\"";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void GetIndividualPscReturns404WhenRegisterViewIsTrueAndEmptyMetrics() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(new MetricsApi()));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void GetIndividualPscReturns404WhenRegisterViewIsTrueAndWrongRegisterMovedTo() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));
        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        registers.setPersonsWithSignificantControl(new RegisterApi().registerMovedTo("wrong"));
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void GetIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndNoMovedToDate() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));
        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        registers.setPersonsWithSignificantControl(new RegisterApi().registerMovedTo("public-register"));
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void GetIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateAndNoCeasedOn() {
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

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void GetIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateEqualsCeasedOn() {
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

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void GetIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateAfterCeasedOn() {
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

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void GetIndividualPscReturns200WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateBeforeCeasedOn() {
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
    void GetIndividualPscReturn200WhenRegisterViewIsFalse() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        Individual individual = new Individual();
        when(transformer.transformPscDocToIndividual(pscDocument, SHOW_FULL_DOB_FALSE)).thenReturn(individual);

        Individual result = service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_FALSE);
        assertEquals(individual, result);
    }

    @Test
    void GetIndividualPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_FALSE));
    }

    @Test
    void GetIndividualBeneficialOwnerPscReturns400WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateEqualsCeasedOn() {
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

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void GetIndividualBeneficialOwnerPscReturns200WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateBeforeCeasedOn() {
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
        when(transformer.transformPscDocToIndividualBeneficialOwner(pscDocument, SHOW_FULL_DOB_TRUE)).thenReturn(individualBo);

        IndividualBeneficialOwner result = service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE);
        assertEquals(individualBo, result);
    }

    @Test
    void GetIndividualBeneficialOwnerPscReturn200WhenRegisterViewIsFalse() {
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
    void GetIndividualBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_FALSE));
    }

    @Test
    void GetWrongTypeIndividualBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_FALSE));
    }

    @Test
    void GetCorporateEntityBeneficialOwnerPscReturn200() {
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
    void GetCorporateEntityBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCorporateEntityBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void GetWrongTypeCorporateBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCorporateEntityBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void GetLegalPersonPscReturn200() {
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
    void GetLegalPersonPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getLegalPersonPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void GetSuperSecurePscReturn200() {
        pscDocument.getData().setKind("super-secure-person-with-significant-control");
        SuperSecure superSecure = new SuperSecure();
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.of(pscDocument));
        when(transformer.transformPscDocToSuperSecure(pscDocument))
                .thenReturn(superSecure);

        SuperSecure result = service
                .getSuperSecurePsc(COMPANY_NUMBER, NOTIFICATION_ID);

        assertEquals(superSecure, result);
    }

    @Test
    void GetSuperSecurePscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSuperSecurePsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void GetWrongTypeSuperSecurePscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSuperSecurePsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void GetSuperSecureBeneficialOwnerPscReturn200() {
        pscDocument.getData().setKind("super-secure-beneficial-owner");
        SuperSecureBeneficialOwner superSecureBeneficialOwner = new SuperSecureBeneficialOwner();
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.of(pscDocument));
        when(transformer.transformPscDocToSuperSecureBeneficialOwner(pscDocument))
                .thenReturn(superSecureBeneficialOwner);

        SuperSecureBeneficialOwner result = service
                .getSuperSecureBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID);

        assertEquals(superSecureBeneficialOwner, result);
    }

    @Test
    void GetSuperSecureBeneficialOwnerPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSuperSecureBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void GetWrongTypeSuperSecureBeneficialOwnerPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSuperSecureBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void GetCorporateEntityPscReturn200() {
        pscDocument.getData().setKind("corporate-entity-person-with-significant-control");
        CorporateEntity corporateEntity = new CorporateEntity();
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.of(pscDocument));
        when(transformer.transformPscDocToCorporateEntity(pscDocument))
                .thenReturn(corporateEntity);

        CorporateEntity result = service.getCorporateEntityPsc(COMPANY_NUMBER, NOTIFICATION_ID);

        assertEquals(corporateEntity, result);
    }

    @Test
    void GetCorporateEntityPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCorporateEntityPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void GetWrongTypeLegalPersonPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getLegalPersonPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void GetLegalPersonBeneficialOwnerPscReturn200() {
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
    void GetLegalPersonBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getLegalPersonBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void GetWrongTypeLegalPersonBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getLegalPersonBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void pscListReturnedByCompanyNumberFromRepository() throws ResourceNotFoundException {
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
        when(repository.getPscDocumentList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(pscDocument)));
        when(transformer.transformPscDocToListSummary(pscDocument, false))
                .thenReturn(listSummary);

        PscList pscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, false, 25);

        assertEquals(expectedPscList, pscDocumentList);
        verify(repository, times(1)).getPscDocumentList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void pscListWithNoMetricsReturnedByCompanyNumberFromRepository() throws ResourceNotFoundException {
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

        when(repository.getPscDocumentList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(pscDocument)));

        when(transformer.transformPscDocToListSummary(pscDocument, false))
                .thenReturn(listSummary);

        PscList pscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, false, 25);

        assertEquals(expectedPscList, pscDocumentList);
        verify(repository, times(1)).getPscDocumentList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void whenNoMetricsDataFoundForCompanyInRegisterViewShouldReturnEmptyList() throws ResourceNotFoundException {
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
    void whenCompanyNotInPublicRegisterGetPSCListShouldThrowNotFound() throws ResourceNotFoundException {
        MetricsApi metricsApi = TestHelper.createMetrics();
        RegistersApi registersApi = new RegistersApi();
        metricsApi.setRegisters(registersApi);

        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.of(metricsApi));

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, true, 25));

        String expectedMessage = "company " + COMPANY_NUMBER + " not on public register";
        String actualMessage = ex.getMessage();
        assertNotNull(actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
        verify(repository, times(0)).getListSummaryRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"), 25);
    }

    @Test
    void pscListReturnedByCompanyNumberFromRepositoryWithExemptions() throws ResourceNotFoundException {
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
        when(repository.getPscDocumentList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(pscDocument)));
        when(transformer.transformPscDocToListSummary(pscDocument, false))
                .thenReturn(listSummary);
        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(Optional.ofNullable(testHelper.createExemptions()));

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
        when(repository.getPscDocumentList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(pscDocument)));
        when(transformer.transformPscDocToListSummary(pscDocument, false))
                .thenReturn(listSummary);

        CompanyExemptions companyExemptions = new CompanyExemptions();
        Exemptions exemptions = new Exemptions();
        exemptions.setPscExemptAsTradingOnUkRegulatedMarket(pscExemptAsTradingOnUkRegulatedMarketItem);
        companyExemptions.setExemptions(testHelper.getUkExemptions());
        when(companyExemptionsApiService.getCompanyExemptions(any())).thenReturn(Optional.ofNullable(testHelper.createExemptions()));

        PscList pscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, false, 25);

        Links links = new Links();
        links.setSelf("/company/" + COMPANY_NUMBER + "/persons-with-significant-control");
        links.setExemptions("/company/" + COMPANY_NUMBER + "/exemptions");

        assertEquals(pscDocumentList.getLinks(), links);
    }

    @Test
    void getIndividualFullRecordShouldReturnFullRecordWhenFound() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(
            Optional.of(pscDocument));
        when(transformer.transformPscDocToIndividualFullRecord(pscDocument)).thenReturn(new IndividualFullRecord());

        service.getIndividualFullRecord(COMPANY_NUMBER, NOTIFICATION_ID);

        // TODO: assert response data matches? need TestHelper to create expected data
        verify(repository).getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID);
        verify(transformer).transformPscDocToIndividualFullRecord(pscDocument);
    }

    @Test
    void getIndividualFullRecordShouldThrowWhenNotFound() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.empty());

        final var exception = assertThrows(ResourceNotFoundException.class,
            () -> service.getIndividualFullRecord(COMPANY_NUMBER, NOTIFICATION_ID));

        assertThat(exception.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(exception.getReason(), is("Individual PSC document not found with id notificationId"));
    }

    @Test
    void getIndividualFullRecordShouldThrowWhenRepositoryThrowsRFNE() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenThrow(
            new ResourceNotFoundException(HttpStatus.NOT_FOUND, "ResourceNotFoundException"));

        final var exception = assertThrows(ResourceNotFoundException.class,
            () -> service.getIndividualFullRecord(COMPANY_NUMBER, NOTIFICATION_ID));

        assertThat(exception.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(exception.getReason(), is("ResourceNotFoundException"));
    }

    @Test
    void getIndividualFullRecordShouldThrowWhenRepositoryThrowsException() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenThrow(
            new NonTransientDataAccessException("NonTransientDataAccessException") {
            });

        final var exception = assertThrows(ResourceNotFoundException.class,
            () -> service.getIndividualFullRecord(COMPANY_NUMBER, NOTIFICATION_ID));

        assertThat(exception.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(exception.getReason(), is("Unexpected error occurred while fetching PSC document"));
    }

    @Test
    void getIndividualFullRecordShouldThrowWhenTransformFails() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(
            Optional.of(pscDocument));
        when(transformer.transformPscDocToIndividualFullRecord(pscDocument)).thenReturn(null);

        final var exception = assertThrows(ResourceNotFoundException.class,
            () -> service.getIndividualFullRecord(COMPANY_NUMBER, NOTIFICATION_ID));

        assertThat(exception.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(exception.getReason(), is("Failed to transform PSCDocument to Individual Full Record"));
    }
}