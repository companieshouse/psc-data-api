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
import uk.gov.companieshouse.pscdataapi.models.Created;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.Updated;
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

    private static final String PSC_ID = "pscId";

    private static final String MOCK_COMPANY_NUMBER = "1234567";

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
        external.setNotificationId(PSC_ID);
        external.setData(data);
        data.setKind("kind");
        internal.setDeltaAt(date);
        request.setInternalData(internal);
        request.setExternalData(external);
        document = new PscDocument();
        document.setUpdated(new Updated().setAt(LocalDate.now()));
        final DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");
        dateString = date.format(dateTimeFormatter);
        document.setCompanyNumber(MOCK_COMPANY_NUMBER);
        document.setPscId(PSC_ID);

    }

    @Test
    void insertBrandNewPscRecordSavesPsc() {
        when(repository.findUpdatedPsc(eq(PSC_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(PSC_ID)).thenReturn(Optional.empty());
        when(transformer.transformPsc(PSC_ID, request)).thenReturn(document);

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
        when(repository.findUpdatedPsc(eq(PSC_ID), dateCaptor.capture())).thenReturn(new ArrayList<>());
        when(repository.findById(PSC_ID)).thenReturn(Optional.of(oldRecord));
        when(transformer.transformPsc(PSC_ID, request)).thenReturn(document);

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
        when(repository.findUpdatedPsc(eq(PSC_ID), dateCaptor.capture())).thenReturn(documents);

        service.insertPscRecord("", request);

        verify(repository, times(0)).save(document);
        assertEquals(dateString, dateCaptor.getValue());
    }

    @Test
    void throwsBadRequestExceptionWhenNotGivenDocument() {
        when(repository.findUpdatedPsc(eq(PSC_ID), any())).thenReturn(new ArrayList<>());
        when(repository.findById(PSC_ID)).thenReturn(Optional.empty());
        when(transformer.transformPsc(PSC_ID, request)).thenReturn(document);
        when(repository.save(document)).thenThrow(new IllegalArgumentException());

        assertThrows(BadRequestException.class, () -> service.insertPscRecord("", request));
    }

    @Test
    void insertNewCreatedWhenCreatedCallToMongoFails() {
        when(repository.findUpdatedPsc(eq(PSC_ID), any())).thenReturn(new ArrayList<>());
        when(repository.findById(PSC_ID)).thenThrow(new RuntimeException());
        when(transformer.transformPsc(PSC_ID, request)).thenReturn(document);

        service.insertPscRecord("", request);

        verify(repository).save(document);
        assertNotNull(document.getCreated().getAt());
    }

    @Test
    @DisplayName("When company number & notification id is provided, delete PSC")
    public void testDeletePSC() {
        when(repository.getPscByCompanyNumberAndId("1234567",PSC_ID)).thenReturn(Optional.ofNullable(document));
        service.deletePsc("1234567",PSC_ID);

        verify(repository, times(1)).getPscByCompanyNumberAndId("1234567",PSC_ID);
        verify(repository, times(1)).delete(document);
    }

    @Test
    @DisplayName("When company number is null throw ResourceNotFound Exception")
    public void testDeletePSCThrowsResourceNotFoundException() {
        when(repository.getPscByCompanyNumberAndId("",PSC_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.deletePsc("",PSC_ID);
        });

        verify(repository, times(1)).getPscByCompanyNumberAndId("",PSC_ID);
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
    public void GetIndividualPscReturn200() throws TransformerException {
        Individual individual = new Individual();
        when(repository.getPscByCompanyNumberAndPscId(MOCK_COMPANY_NUMBER,PSC_ID)).thenReturn(Optional.of(document));
        when(transformer.transformPscDocToIndividual(Optional.of(document))).thenReturn(individual);

        Individual result = service.getIndividualPsc(MOCK_COMPANY_NUMBER,PSC_ID);

        assertEquals(individual,result);
    }

    @Test
    public void GetIndividualPscReturn404() {
        when(repository.getPscByCompanyNumberAndPscId(MOCK_COMPANY_NUMBER,PSC_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getIndividualPsc(MOCK_COMPANY_NUMBER,PSC_ID);
        });
    }

}