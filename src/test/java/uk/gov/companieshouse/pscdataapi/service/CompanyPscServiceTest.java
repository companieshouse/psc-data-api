package uk.gov.companieshouse.pscdataapi.service;

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
import uk.gov.companieshouse.api.api.CompanyMetricsApiService;
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.psc.*;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.models.*;
import uk.gov.companieshouse.pscdataapi.repository.CompanyPscRepository;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private FullRecordCompanyPSCApi request;
    private PscDocument document;
    private String dateString;
    private OffsetDateTime date;
    private OffsetDateTime laterDate;

    @BeforeEach
    public void setUp() {
        date = TestHelper.createOffsetDateTime();
        dateString = date.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS"));
        laterDate = TestHelper.createLaterOffsetDateTime();

        request = TestHelper.buildBasicFullRecordPsc();
        document = TestHelper.buildBasicDocument();
    }

    @Test
    void insertBrandNewPscRecordSavesPsc() {
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        when(transformer.transformPscOnInsert(NOTIFICATION_ID, request)).thenReturn(document);

        service.insertPscRecord("", request);

        verify(repository).save(document);
        Assertions.assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(document.getCreated().getAt());
    }

    @Test
    void insertUpdatePscRecordSavesPsc() {
        PscDocument oldRecord = new PscDocument();
        LocalDateTime date = LocalDateTime.now();
        oldRecord.setCreated(new Created().setAt(date));
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(oldRecord));
        when(transformer.transformPscOnInsert(NOTIFICATION_ID, request)).thenReturn(document);

        service.insertPscRecord("", request);

        verify(repository).save(document);
        Assertions.assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(document.getCreated().getAt());
        Assertions.assertEquals(date, document.getCreated().getAt());
    }

    @Test
    void insertPscRecordDoesNotSavePscWhenUpdateAlreadyMade() {

        List<PscDocument> documents = new ArrayList<>();
        documents.add(new PscDocument());
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(documents);

        service.insertPscRecord("", request);

        verify(repository, times(0)).save(document);
        Assertions.assertEquals(dateString, dateCaptor.getValue());
    }

    @Test
    void throwsBadRequestExceptionWhenNotGivenDocument() {
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), any())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        when(transformer.transformPscOnInsert(NOTIFICATION_ID, request)).thenReturn(document);
        when(repository.save(document)).thenThrow(new IllegalArgumentException());

        assertThrows(BadRequestException.class, () -> service.insertPscRecord("", request));
    }

    @Test
    void insertNewCreatedWhenCreatedCallToMongoFails() {
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), any())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenThrow(new RuntimeException());
        when(transformer.transformPscOnInsert(NOTIFICATION_ID, request)).thenReturn(document);

        service.insertPscRecord("", request);

        verify(repository).save(document);
        assertNotNull(document.getCreated().getAt());
    }

    @Test
    @DisplayName("When company number & notification id is provided, delete PSC")
    public void testDeletePSC() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.ofNullable(document));
        service.deletePsc(COMPANY_NUMBER,NOTIFICATION_ID);

        verify(repository, times(1)).getPscByCompanyNumberAndId(COMPANY_NUMBER,NOTIFICATION_ID);
        verify(repository, times(1)).delete(document);
    }

    @Test
    @DisplayName("When company number is null throw ResourceNotFound Exception")
    public void testDeletePSCThrowsResourceNotFoundException() {
        when(repository.getPscByCompanyNumberAndId("",NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deletePsc("",NOTIFICATION_ID));

        verify(repository, times(1)).getPscByCompanyNumberAndId("",NOTIFICATION_ID);
        verify(repository, times(0)).delete(any());
    }

    @Test
    @DisplayName("When company number and id is null throw ResourceNotFound Exception")
    public void testDeletePSCThrowsResourceNotFoundExceptionWhenCompanyNumberAndNotificationIdIsNull() {
        when(repository.getPscByCompanyNumberAndId("","")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deletePsc("",""));

        verify(repository, times(1)).getPscByCompanyNumberAndId("","");
        verify(repository, times(0)).delete(any());
    }

    @Test
    public void GetIndividualPscReturns404WhenRegisterViewIsTrueAndNoMetrics() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"No company metrics data found for company number: " + COMPANY_NUMBER +"\"";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    public void GetIndividualPscReturns404WhenRegisterViewIsTrueAndEmptyMetrics() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(new MetricsApi()));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    public void GetIndividualPscReturns404WhenRegisterViewIsTrueAndWrongRegisterMovedTo() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));
        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        registers.setPersonsWithSignificantControl(new RegisterApi().registerMovedTo("wrong"));
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    public void GetIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndNoMovedToDate() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));
        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        registers.setPersonsWithSignificantControl(new RegisterApi().registerMovedTo("public-register"));
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    public void GetIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateAndNoCeasedOn() {
        PscData pscData = document.getData();
        pscData.setCeased(true);
        document.setData(pscData);
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(date);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    public void GetIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateEqualsCeasedOn() {
        PscData pscData = document.getData();
        pscData.setCeased(true);
        pscData.setCeasedOn(date.toLocalDate());
        document.setData(pscData);
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(date);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    public void GetIndividualPscReturns404WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateAfterCeasedOn() {
        PscData pscData = document.getData();
        pscData.setCeased(true);
        pscData.setCeasedOn(date.toLocalDate());
        document.setData(pscData);
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(laterDate);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    public void GetIndividualPscReturns200WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateBeforeCeasedOn() {
        PscData pscData = document.getData();
        pscData.setCeased(true);
        pscData.setCeasedOn(laterDate.toLocalDate());
        document.setData(pscData);
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(date);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        Individual individual = new Individual();
        when(transformer.transformPscDocToIndividual(document, SHOW_FULL_DOB_TRUE)).thenReturn(individual);

        Individual result = service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_TRUE);
        Assertions.assertEquals(individual, result);
    }

    @Test
    public void GetIndividualPscReturn200WhenRegisterViewIsFalse() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));

        Individual individual = new Individual();
        when(transformer.transformPscDocToIndividual(document, SHOW_FULL_DOB_FALSE)).thenReturn(individual);

        Individual result = service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_FALSE);
        Assertions.assertEquals(individual, result);
    }

    @Test
    public void GetIndividualPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_FALSE));
    }

    @Test
    public void GetWrongTypePscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getIndividualPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_FALSE));
    }

    @Test
    public void GetIndividualBeneficialOwnerPscReturns400WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateEqualsCeasedOn() {
        PscData pscData = document.getData();
        pscData.setCeased(true);
        pscData.setCeasedOn(date.toLocalDate());
        pscData.setKind(TestHelper.INDIVIDUAL_BO_KIND);
        document.setData(pscData);
        when(repository.findById(NOTIFICATION_ID))
                .thenReturn(Optional.of(document));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(date);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_TRUE));
        String expectedErrorMessage = "404 NOT_FOUND \"not-on-public-register\"";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    public void GetIndividualBeneficialOwnerPscReturns200WhenRegisterViewIsTrueAndOnPublicRegisterAndMovedToDateBeforeCeasedOn() {
        PscData pscData = document.getData();
        pscData.setCeased(true);
        pscData.setCeasedOn(laterDate.toLocalDate());
        pscData.setKind(TestHelper.INDIVIDUAL_BO_KIND);
        document.setData(pscData);
        when(repository.findById(NOTIFICATION_ID))
                .thenReturn(Optional.of(document));

        MetricsApi metrics = new MetricsApi();
        RegistersApi registers = new RegistersApi();
        RegisterApi pscRegisters = new RegisterApi();
        pscRegisters.registerMovedTo("public-register");
        pscRegisters.setMovedOn(date);
        registers.setPersonsWithSignificantControl(pscRegisters);
        metrics.setRegisters(registers);
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER)).thenReturn(Optional.of(metrics));

        IndividualBeneficialOwner individualBo = new IndividualBeneficialOwner();
        when(transformer.transformPscDocToIndividualBeneficialOwner(document, SHOW_FULL_DOB_TRUE)).thenReturn(individualBo);

        IndividualBeneficialOwner result = service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_TRUE);
        Assertions.assertEquals(individualBo, result);
    }

    @Test
    public void GetIndividualBeneficialOwnerPscReturn200WhenRegisterViewIsFalse() {
        document.getData().setKind(TestHelper.INDIVIDUAL_BO_KIND);
        IndividualBeneficialOwner individualBeneficialOwner = new IndividualBeneficialOwner();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToIndividualBeneficialOwner(document, SHOW_FULL_DOB_FALSE))
                .thenReturn(individualBeneficialOwner);

        IndividualBeneficialOwner result = service
                .getIndividualBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_FALSE);

        Assertions.assertEquals(individualBeneficialOwner, result);
    }

    @Test
    public void GetIndividualBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID, REGISTER_VIEW_FALSE));
    }

    @Test
    public void GetWrongTypeIndividualBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getIndividualBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID,REGISTER_VIEW_FALSE));
    }
    @Test
    public void GetCorporateEntityBeneficialOwnerPscReturn200() {
        document.getData().setKind("corporate-entity-beneficial-owner");
        CorporateEntityBeneficialOwner corporateEntityBeneficialOwner =
                new CorporateEntityBeneficialOwner();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToCorporateEntityBeneficialOwner(document))
                .thenReturn(corporateEntityBeneficialOwner);

        CorporateEntityBeneficialOwner result = service
                .getCorporateEntityBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID);

        Assertions.assertEquals(corporateEntityBeneficialOwner, result);
    }

    @Test
    public void GetCorporateEntityBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCorporateEntityBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID));
    }

    @Test
    public void GetWrongTypeCorporateBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCorporateEntityBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID));
    }

    @Test
    public void GetLegalPersonPscReturn200() {
        document.getData().setKind("legal-person-person-with-significant-control");
        LegalPerson legalPerson =
                new LegalPerson();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToLegalPerson(document))
                .thenReturn(legalPerson);

        LegalPerson result = service
                .getLegalPersonPsc(COMPANY_NUMBER,NOTIFICATION_ID);

        Assertions.assertEquals(legalPerson, result);
    }

    @Test
    public void GetLegalPersonPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getLegalPersonPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    public void GetSuperSecurePscReturn200() {
        document.getData().setKind("super-secure-person-with-significant-control");
        SuperSecure superSecure = new SuperSecure();
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToSuperSecure(document))
                .thenReturn(superSecure);

        SuperSecure result = service
                .getSuperSecurePsc(COMPANY_NUMBER,NOTIFICATION_ID);

        Assertions.assertEquals(superSecure, result);
    }

    @Test
    public void GetSuperSecurePscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSuperSecurePsc(COMPANY_NUMBER,NOTIFICATION_ID));
    }

    @Test
    public void GetWrongTypeSuperSecurePscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER,NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSuperSecurePsc(COMPANY_NUMBER,NOTIFICATION_ID));
    }

    @Test
    public void GetSuperSecureBeneficialOwnerPscReturn200() {
        document.getData().setKind("super-secure-beneficial-owner");
        SuperSecureBeneficialOwner superSecureBeneficialOwner = new SuperSecureBeneficialOwner();
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToSuperSecureBeneficialOwner(document))
                .thenReturn(superSecureBeneficialOwner);

        SuperSecureBeneficialOwner result = service
                .getSuperSecureBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID);

        Assertions.assertEquals(superSecureBeneficialOwner, result);
    }

    @Test
    public void GetSuperSecureBeneficialOwnerPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSuperSecureBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID));
    }

    @Test
    public void GetWrongTypeSuperSecureBeneficialOwnerPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER,NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSuperSecureBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID));
    }

    @Test
    public void GetCorporateEntityPscReturn200() {
        document.getData().setKind("corporate-entity-person-with-significant-control");
        CorporateEntity corporateEntity = new CorporateEntity();
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToCorporateEntity(document))
                .thenReturn(corporateEntity);

        CorporateEntity result = service.getCorporateEntityPsc(COMPANY_NUMBER,NOTIFICATION_ID);

        Assertions.assertEquals(corporateEntity, result);
    }

    @Test
    public void GetCorporateEntityPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCorporateEntityPsc(COMPANY_NUMBER,NOTIFICATION_ID));
    }

    @Test
    public void GetWrongTypeLegalPersonPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getLegalPersonPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    public void GetWrongTypeCorporateEntityPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCorporateEntityPsc(COMPANY_NUMBER,NOTIFICATION_ID));
    }

    @Test
    public void GetLegalPersonBeneficialOwnerPscReturn200() {
        document.getData().setKind("legal-person-beneficial-owner");
        LegalPersonBeneficialOwner legalPersonBeneficialOwner =
                new LegalPersonBeneficialOwner();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToLegalPersonBeneficialOwner(document))
                .thenReturn(legalPersonBeneficialOwner);

        LegalPersonBeneficialOwner result = service
                .getLegalPersonBeneficialOwnerPsc(COMPANY_NUMBER,NOTIFICATION_ID);

        Assertions.assertEquals(legalPersonBeneficialOwner, result);
    }

    @Test
    public void GetLegalPersonBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getLegalPersonBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    public void GetWrongTypeLegalPersonBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getLegalPersonBeneficialOwnerPsc(COMPANY_NUMBER, NOTIFICATION_ID));
    }

    @Test
    void whenNoPSCExistGetPSCListShouldThrow() {
        assertThrows(ResourceNotFoundException.class, ()-> service.retrievePscListSummaryFromDb( COMPANY_NUMBER, 0, false,25));
    }

    @Test
    void pscListReturnedByCompanyNumberFromRepository() throws ResourceNotFoundException {
        PscList expectedPscList = TestHelper.createPscList();
        PscData pscData = new PscData();
        document.setData(pscData);
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
        when(repository.getPscDocumentList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));
        when(transformer.transformPscDocToListSummary(document, false))
                .thenReturn(listSummary);

        PscList PscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER,0, false,25);

        Assertions.assertEquals(expectedPscList, PscDocumentList);
        verify(repository, times(1)).getPscDocumentList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void pscListWithNoMetricsReturnedByCompanyNumberFromRepository() throws ResourceNotFoundException {
        PscList expectedPscList = TestHelper.createPscListWithNoMetrics();
        PscData pscData = new PscData();
        document.setData(pscData);
        document.setId(TestHelper.PSC_ID);

        ListSummary listSummary = new ListSummary();
        Identification identification = new Identification();
        identification.setPlaceRegistered("x");
        identification.setCountryRegistered("x");
        identification.setRegistrationNumber("x");
        identification.setLegalAuthority("x");
        identification.setLegalForm("x");
        listSummary.setIdentification(identification);

        when(repository.getPscDocumentList(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(Collections.singletonList(document)));

        when(transformer.transformPscDocToListSummary(document, false))
                .thenReturn(listSummary);

        PscList PscDocumentList = service.retrievePscListSummaryFromDb(COMPANY_NUMBER,0, false,25);

        Assertions.assertEquals(expectedPscList, PscDocumentList);
        verify(repository, times(1)).getPscDocumentList(COMPANY_NUMBER, 0, 25);
    }

    @Test
    void whenNoMetricsDataFoundForCompanyInRegisterViewShouldThrow() throws ResourceNotFoundException {
        when(companyMetricsApiService.getCompanyMetrics(COMPANY_NUMBER))
                .thenReturn(Optional.empty());

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> service.retrievePscListSummaryFromDb(COMPANY_NUMBER,0, true,25));

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

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> service.retrievePscListSummaryFromDb(COMPANY_NUMBER,0, true,25));

        String expectedMessage = "company " + COMPANY_NUMBER + " not on public register";
        String actualMessage = ex.getMessage();
        assertNotNull(actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
        verify(service, times(1)).retrievePscListSummaryFromDb(COMPANY_NUMBER,0, true, 25);
        verify(repository, times(0)).getListSummaryRegisterView(COMPANY_NUMBER, 0, OffsetDateTime.parse("2020-12-20T06:00Z"), 25);
    }

}