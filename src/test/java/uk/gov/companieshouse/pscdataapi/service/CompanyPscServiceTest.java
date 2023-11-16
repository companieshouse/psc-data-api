package uk.gov.companieshouse.pscdataapi.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.psc.*;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.models.*;
import uk.gov.companieshouse.pscdataapi.repository.CompanyPscRepository;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;

import javax.xml.transform.TransformerException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyPscServiceTest {

    private static final String NOTIFICATION_ID = "pscId";

    private static final String MOCK_COMPANY_NUMBER = "1234567";

    private static final Boolean MOCK_REGISTER_TRUE = true;

    private static final Boolean MOCK_REGISTER_FALSE = false;

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

    @InjectMocks
    private CompanyPscService service;

    private FullRecordCompanyPSCApi request;
    private PscDocument document;
    private String dateString;

    @BeforeEach
    public void setUp() {
        OffsetDateTime date = OffsetDateTime.now();
        request = new FullRecordCompanyPSCApi();
        InternalData internal = new InternalData();
        ExternalData external = new ExternalData();
        Data data = new Data();
        external.setNotificationId(NOTIFICATION_ID);
        external.setData(data);
        data.setKind("individual-person-with-significant-control");
        internal.setDeltaAt(date);
        request.setInternalData(internal);
        request.setExternalData(external);
        document = new PscDocument();
        document.setUpdated(new Updated().setAt(LocalDate.now()));
        final DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");
        dateString = date.format(dateTimeFormatter);
        document.setCompanyNumber(MOCK_COMPANY_NUMBER);
        document.setPscId("1234");
        PscData pscData = new PscData();
        pscData.setKind("individual-person-with-significant-control");
        document.setNotificationId(MOCK_COMPANY_NUMBER);
        document.setData(pscData);
        Identification identification = new Identification();
        identification.setCountryRegistered("x");
        identification.setLegalForm("x");
        identification.setPlaceRegistered("x");
        identification.setLegalAuthority("x");
        identification.setRegistrationNumber("x");
        document.setIdentification(new PscIdentification(identification));


    }

    @Test
    void insertBrandNewPscRecordSavesPsc() {
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        when(transformer.transformPsc(NOTIFICATION_ID, request)).thenReturn(document);

        service.insertPscRecord("", request);

        verify(repository).save(document);
        assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(document.getCreated().getAt());
    }

    @Test
    void insertUpdatePscRecordSavesPsc() {
        PscDocument oldRecord = new PscDocument();
        LocalDateTime date = LocalDateTime.now();
        oldRecord.setCreated(new Created().setAt(date));
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(oldRecord));
        when(transformer.transformPsc(NOTIFICATION_ID, request)).thenReturn(document);

        service.insertPscRecord("", request);

        verify(repository).save(document);
        assertEquals(dateString, dateCaptor.getValue());
        assertNotNull(document.getCreated().getAt());
        assertEquals(date, document.getCreated().getAt());
    }

    @Test
    void insertPscRecordDoesNotSavePscWhenUpdateAlreadyMade() {

        List<PscDocument> documents = new ArrayList<>();
        documents.add(new PscDocument());
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), dateCaptor.capture())).thenReturn(documents);

        service.insertPscRecord("", request);

        verify(repository, times(0)).save(document);
        assertEquals(dateString, dateCaptor.getValue());
    }

    @Test
    void throwsBadRequestExceptionWhenNotGivenDocument() {
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), any())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        when(transformer.transformPsc(NOTIFICATION_ID, request)).thenReturn(document);
        when(repository.save(document)).thenThrow(new IllegalArgumentException());

        assertThrows(BadRequestException.class, () -> service.insertPscRecord("", request));
    }

    @Test
    void insertNewCreatedWhenCreatedCallToMongoFails() {
        when(repository.findUpdatedPsc(eq(NOTIFICATION_ID), any())).thenReturn(new ArrayList<>());
        when(repository.findById(NOTIFICATION_ID)).thenThrow(new RuntimeException());
        when(transformer.transformPsc(NOTIFICATION_ID, request)).thenReturn(document);

        service.insertPscRecord("", request);

        verify(repository).save(document);
        assertNotNull(document.getCreated().getAt());
    }

    @Test
    @DisplayName("When company number & notification id is provided, delete PSC")
    public void testDeletePSC() {
        when(repository.getPscByCompanyNumberAndId("1234567",NOTIFICATION_ID)).thenReturn(Optional.ofNullable(document));
        service.deletePsc("1234567",NOTIFICATION_ID);

        verify(repository, times(1)).getPscByCompanyNumberAndId("1234567",NOTIFICATION_ID);
        verify(repository, times(1)).delete(document);
    }

    @Test
    @DisplayName("When company number is null throw ResourceNotFound Exception")
    public void testDeletePSCThrowsResourceNotFoundException() {
        when(repository.getPscByCompanyNumberAndId("",NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.deletePsc("",NOTIFICATION_ID);
        });

        verify(repository, times(1)).getPscByCompanyNumberAndId("",NOTIFICATION_ID);
        verify(repository, times(0)).delete(any());
    }

    @Test
    @DisplayName("When company number and id is null throw ResourceNotFound Exception")
    public void testDeletePSCThrowsResourceNotFoundExceptionWhenCompanyNumberAndNotificationIdIsNull() {
        when(repository.getPscByCompanyNumberAndId("","")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.deletePsc("","");
        });

        verify(repository, times(1)).getPscByCompanyNumberAndId("","");
        verify(repository, times(0)).delete(any());
    }

    @Test
    public void GetIndividualPscReturn200WhenRegisterViewIsTrue() throws TransformerException {
        Individual individual = new Individual();
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));
        when(transformer.transformPscDocToIndividual(Optional.of(document), MOCK_REGISTER_TRUE)).thenReturn(individual);

        Individual result = service.getIndividualPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID, MOCK_REGISTER_TRUE);

        assertEquals(individual,result);
    }

    @Test
    public void GetIndividualPscReturn200WhenRegisterViewIsFalse() throws TransformerException {
        Individual individual = new Individual();
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.of(document));
        when(transformer.transformPscDocToIndividual(Optional.of(document), MOCK_REGISTER_FALSE)).thenReturn(individual);

        Individual result = service.getIndividualPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID, MOCK_REGISTER_FALSE);

        assertEquals(individual,result);
    }

    @Test
    public void GetIndividualPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getIndividualPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID,MOCK_REGISTER_FALSE);
        });
    }

    @Test
    public void GetWrongTypePscReturn404() {
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getIndividualPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID,MOCK_REGISTER_FALSE);
        });
    }

    @Test
    public void GetIndividualBeneficialOwnerPscReturn200() throws TransformerException {
        document.getData().setKind("individual-beneficial-owner");
        IndividualBeneficialOwner individualBeneficialOwner = new IndividualBeneficialOwner();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToIndividualBeneficialOwner(Optional.of(document),MOCK_REGISTER_FALSE))
                .thenReturn(individualBeneficialOwner);

        IndividualBeneficialOwner result = service
                .getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID,MOCK_REGISTER_FALSE);

        assertEquals(individualBeneficialOwner,result);
    }

    @Test
    public void GetIndividualBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID,MOCK_REGISTER_FALSE);
        });
    }

    @Test
    public void GetWrongTypeIndividualBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID,MOCK_REGISTER_FALSE);
        });
    }
    @Test
    public void GetCorporateEntityBeneficialOwnerPscReturn200() throws TransformerException {
        document.getData().setKind("corporate-entity-beneficial-owner");
        CorporateEntityBeneficialOwner corporateEntityBeneficialOwner =
                new CorporateEntityBeneficialOwner();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToCorporateEntityBeneficialOwner(Optional.of(document)))
                .thenReturn(corporateEntityBeneficialOwner);

        CorporateEntityBeneficialOwner result = service
                .getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);

        assertEquals(corporateEntityBeneficialOwner,result);
    }

    @Test
    public void GetCorporateEntityBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);
        });
    }

    @Test
    public void GetWrongTypeCorporateBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);
        });
    }

    @Test
    public void GetLegalPersonPscReturn200() throws TransformerException {
        document.getData().setKind("legal-person-person-with-significant-control");
        LegalPerson legalPerson =
                new LegalPerson();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToLegalPerson(Optional.of(document)))
                .thenReturn(legalPerson);

        LegalPerson result = service
                .getLegalPersonPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);

        assertEquals(legalPerson,result);
    }

    @Test
    public void GetLegalPersonPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            service.getLegalPersonPsc(MOCK_COMPANY_NUMBER, NOTIFICATION_ID);
        });
    }

    @Test
    public void GetSuperSecurePscReturn200() throws TransformerException {
        document.getData().setKind("super-secure-person-with-significant-control");
        SuperSecure superSecure = new SuperSecure();
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToSuperSecure(Optional.of(document)))
                .thenReturn(superSecure);

        SuperSecure result = service
                .getSuperSecurePsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);

        assertEquals(superSecure,result);
    }

    @Test
    public void GetSuperSecurePscReturn404() {
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getSuperSecurePsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);
        });
    }

    @Test
    public void GetWrongTypeSuperSecurePscReturn404() {
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER,NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getSuperSecurePsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);
        });
    }

    @Test
    public void GetSuperSecureBeneficialOwnerPscReturn200() throws TransformerException {
        document.getData().setKind("super-secure-beneficial-owner");
        SuperSecureBeneficialOwner superSecureBeneficialOwner = new SuperSecureBeneficialOwner();
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToSuperSecureBeneficialOwner(Optional.of(document)))
                .thenReturn(superSecureBeneficialOwner);

        SuperSecureBeneficialOwner result = service
                .getSuperSecureBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);

        assertEquals(superSecureBeneficialOwner,result);
    }

    @Test
    public void GetSuperSecureBeneficialOwnerPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getSuperSecureBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);
        });
    }

    @Test
    public void GetWrongTypeSuperSecureBeneficialOwnerPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER,NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getSuperSecureBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);
        });
    }

    @Test
    public void GetCorporateEntityPscReturn200() throws TransformerException {
        document.getData().setKind("corporate-entity-person-with-significant-control");
        CorporateEntity corporateEntity = new CorporateEntity();
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER,NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToCorporateEntity(Optional.of(document)))
                .thenReturn(corporateEntity);

        CorporateEntity result = service.getCorporateEntityPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);

        assertEquals(corporateEntity,result);
    }

    @Test
    public void GetCorporateEntityPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getCorporateEntityPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);
        });
    }

    @Test
    public void GetWrongTypeLegalPersonPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getLegalPersonPsc(MOCK_COMPANY_NUMBER, NOTIFICATION_ID);
        });
    }

    public void GetWrongTypeCorporateEntityPscReturn404() {
        when(repository.getPscByCompanyNumberAndId(MOCK_COMPANY_NUMBER, NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getCorporateEntityPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);
        });

    }

    @Test
    public void GetLegalPersonBeneficialOwnerPscReturn200() throws TransformerException {
        document.getData().setKind("legal-person-beneficial-owner");
        LegalPersonBeneficialOwner legalPersonBeneficialOwner =
                new LegalPersonBeneficialOwner();
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToLegalPersonBeneficialOwner(Optional.of(document)))
                .thenReturn(legalPersonBeneficialOwner);

        LegalPersonBeneficialOwner result = service
                .getLegalPersonBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,NOTIFICATION_ID);

        assertEquals(legalPersonBeneficialOwner,result);
    }

    @Test
    public void GetLegalPersonBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            service.getLegalPersonBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, NOTIFICATION_ID);
        });
    }

    @Test
    public void GetWrongTypeLegalPersonBeneficialOwnerPscReturn404() {
        when(repository.findById(NOTIFICATION_ID)
                .filter(document -> document.getData().getKind()
                        .equals("WRONG KIND")))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getLegalPersonBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, NOTIFICATION_ID);
        });
    }

}