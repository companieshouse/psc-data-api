package uk.gov.companieshouse.pscdataapi.service;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.api.api.CompanyMetricsApiService;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.exemptions.Exemptions;
import uk.gov.companieshouse.api.exemptions.PscExemptAsTradingOnRegulatedMarketItem;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Identification;
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
import uk.gov.companieshouse.pscdataapi.exceptions.ResourceNotFoundException;
import uk.gov.companieshouse.pscdataapi.models.Created;
import uk.gov.companieshouse.pscdataapi.models.PscData;
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
    @Captor
    private ArgumentCaptor<String> dateCaptor;
    @Spy
    @InjectMocks
    private CompanyPscService service;
    @Mock
    CompanyMetricsApiService companyMetricsApiService;

    @Mock
    CompanyExemptionsApiService companyExemptionsApiService;

    private FullRecordCompanyPSCApi request;
    private PscDocument pscDocument;
    private String dateString;
    private OffsetDateTime date;
    private OffsetDateTime laterDate;

    private TestHelper testHelper;

    private PscExemptAsTradingOnRegulatedMarketItem pscExemptAsTradingOnRegulatedMarketItem;

    @BeforeEach
    void setUp() {
        date = TestHelper.createOffsetDateTime();
        dateString = date.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS"));
        laterDate = TestHelper.createLaterOffsetDateTime();

        request = TestHelper.buildBasicFullRecordPsc();
        pscDocument = TestHelper.buildBasicDocument();
        testHelper = new TestHelper();
        pscExemptAsTradingOnRegulatedMarketItem = new PscExemptAsTradingOnRegulatedMarketItem();
    }

    @Test
    void insertBrandNewPscRecordSavesPsc() {
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        when(transformer.transformPscOnInsert(NOTIFICATION_ID, request)).thenReturn(pscDocument);

        service.insertPscRecord("", request);

        verify(repository).save(pscDocument);
        Assertions.assertEquals(dateString, dateCaptor.getValue());
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
        Assertions.assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(pscDocument.getCreated().getAt());
        Assertions.assertEquals(localDateTime, pscDocument.getCreated().getAt());
    }

    @Test
    void insertPscRecordDoesNotSavePscWhenUpdateAlreadyMade() {

        List<PscDocument> documents = new ArrayList<>();
        documents.add(new PscDocument());
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(documents);

        service.insertPscRecord("", request);

        verify(repository, times(0)).save(pscDocument);
        Assertions.assertEquals(dateString, dateCaptor.getValue());
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
        assertNotNull(pscDocument.getCreated().getAt());
    }

    @Test
    @DisplayName("When company number & notification id is provided, delete PSC")
    void testDeletePSC() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID)).thenReturn(Optional.ofNullable(pscDocument));
        service.deletePsc(COMPANY_NUMBER, NOTIFICATION_ID, "");

        verify(repository, times(1)).getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID);
        verify(repository, times(1)).delete(pscDocument);
    }

    @Test
    @DisplayName("When company number is null throw ResourceNotFound Exception")
    void testDeletePSCThrowsResourceNotFoundException() {
        when(repository.getPscByCompanyNumberAndId("", NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deletePsc("", NOTIFICATION_ID, ""));

        verify(repository, times(1)).getPscByCompanyNumberAndId("", NOTIFICATION_ID);
        verify(repository, times(0)).delete(any());
    }

    @Test
    @DisplayName("When company number and id is null throw ResourceNotFound Exception")
    void testDeletePSCThrowsNotFoundExceptionWhenCompanyNumberAndNotificationIdIsNull() {
        when(repository.getPscByCompanyNumberAndId("", "")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deletePsc("", "", ""));

        verify(repository, times(1)).getPscByCompanyNumberAndId("", "");
        verify(repository, times(0)).delete(any());
    }

    @Test
    void GetIndividualPscReturns404WhenRegisterViewIsTrueAndNoMetrics() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"No company metrics data found for company number: " + COMPANY_NUMBER + "\"";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void GetIndividualPscReturns404WhenRegisterViewIsTrueAndEmptyMetrics() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(new MetricsApi()));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
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
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
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
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
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
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
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
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
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
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
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
        Assertions.assertEquals(individual, result);
    }

    @Test
    void GetIndividualPscReturn200WhenRegisterViewIsFalse() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(pscDocument));

        Individual individual = new Individual();
        when(transformer.transformPscDocToIndividual(pscDocument, SHOW_FULL_DOB_FALSE)).thenReturn(individual);

        Individual result = service.getIndividualPsc(COMPANY_NUMBER, NOTIFICATION_ID, REGISTER_VIEW_FALSE);
        Assertions.assertEquals(individual, result);
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
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
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
        Assertions.assertEquals(individualBo, result);
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

        Assertions.assertEquals(individualBeneficialOwner, result);
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

        Assertions.assertEquals(corporateEntityBeneficialOwner, result);
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

        Assertions.assertEquals(legalPerson, result);
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

        Assertions.assertEquals(superSecure, result);
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

        Assertions.assertEquals(superSecureBeneficialOwner, result);
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

        Assertions.assertEquals(corporateEntity, result);
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

        Assertions.assertEquals(legalPersonBeneficialOwner, result);
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
    void whenNoPSCExistGetPSCListShouldThrow() {
        assertThrows(ResourceNotFoundException.class, () -> service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, false, 25));
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

//        CompanyExemptions companyExemptions = new CompanyExemptions();
//        Exemptions exemptions = new Exemptions();
//        companyExemptions.setExemptions(exemptions);
//        Optional<CompanyExemptions> optionalExempt = Optional.of(companyExemptions);
//        when(companyExemptionsApiService.getCompanyExemptions(COMPANY_NUMBER)).thenReturn(optionalExempt);

        PscList PscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, false, 25);

        Assertions.assertEquals(expectedPscList, PscDocumentList);
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

        PscList PscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, false, 25);

        Assertions.assertEquals(expectedPscList, PscDocumentList);
        verify(repository, times(1)).getPscDocumentList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void whenNoMetricsDataFoundForCompanyInRegisterViewShouldThrow() throws ResourceNotFoundException {
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.empty());

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> service.retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, true, 25));

        String expectedMessage = "No company metrics data found for company number: " + COMPANY_NUMBER;
        String actualMessage = ex.getMessage();
        assertNotNull(actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
        verify(repository, times(0)).getListSummaryRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"), 25);
    }

    @Test
    void whenCompanyNotInPublicRegisterGetPSCListShouldThrow() throws ResourceNotFoundException {
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
        verify(service, times(1)).retrievePscListSummaryFromDb(COMPANY_NUMBER, 0, true, 25);
        verify(repository, times(0)).getListSummaryRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"), 25);
    }

}