package uk.gov.companieshouse.pscdataapi.controller;

import io.cucumber.java.sl.In;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.api.psc.*;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.Updated;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyPscControllerTest {
    private static final String PUT_URL =
            "/company/123456789/persons-with-significant-control/123456789/full_record";

    private static final String GET_URL =
            "/company/123456789/persons-with-significant-control/individual/123456789";

    private static final String GET_IndividualBeneficialOwner_URL =
            "/company/123456789/persons-with-significant-control/individual-beneficial-owner/123456789";

    private static final String GET_CorporateEntityBeneficialOwner_URL =
            "/company/123456789/persons-with-significant-control/corporate-entity-beneficial-owner/123456789";

    private static final String GET_Legal_Person_URL =
            "/company/123456789/persons-with-significant-control/legal-person/123456789";

    private static final String X_REQUEST_ID = "123456";

    private static final String MOCK_COMPANY_NUMBER = "123456789";

    private static final String MOCK_NOTIFICATION_ID = "123456789";

    private static final String ERIC_IDENTITY = "Test-Identity";
    private static final String ERIC_IDENTITY_TYPE = "key";
    private static final String ERIC_PRIVILEGES = "*";
    //private static final String X_REQUEST_ID = TestHelper.X_REQUEST_ID;

    private static final String DELETE_URL = String.format("/company/%s/persons-with-significant-control/%s/full_record", MOCK_COMPANY_NUMBER, MOCK_COMPANY_NUMBER);

    private FullRecordCompanyPSCApi request;

    private PscDocument document;

    private Individual individual;

    private IndividualBeneficialOwner individualBeneficialOwner;

    private CorporateEntityBeneficialOwner corporateEntityBeneficialOwner;

    private LegalPerson legalPerson;

    private String dateString;

    @MockBean
    private CompanyPscService companyPscService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompanyPscController companyPscController;



    @BeforeEach
    public void setUp() {
        OffsetDateTime date = OffsetDateTime.now();
        request = new FullRecordCompanyPSCApi();
        InternalData internal = new InternalData();
        ExternalData external = new ExternalData();
        Data data = new Data();
        external.setNotificationId(MOCK_NOTIFICATION_ID);
        external.setData(data);
        data.setKind("kind");
        internal.setDeltaAt(date);
        request.setInternalData(internal);
        request.setExternalData(external);
        document = new PscDocument();
        document.setCompanyNumber(MOCK_COMPANY_NUMBER);
        document.setNotificationId(MOCK_NOTIFICATION_ID);
        document.setUpdated(new Updated().setAt(LocalDate.now()));
        final DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");
        dateString = date.format(dateTimeFormatter);


    }

    @Test
    void contextLoads() {
        assertThat(companyPscController).isNotNull();
    }


    @Test
    void callPutRequest() throws Exception {
        doNothing()
                .when(companyPscService).insertPscRecord(anyString(), isA(FullRecordCompanyPSCApi.class));

        mockMvc.perform(put(PUT_URL)
                .contentType(APPLICATION_JSON)
                .header("x-request-id", X_REQUEST_ID)
                .header("ERIC-Identity", "test")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Roles", "*")
                .content(TestHelper.createJsonPayload()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Return 401 when no api key is present")
    void deletePSCWhenNoApiKeyPresent() throws Exception {
        mockMvc.perform(delete(PUT_URL)).andExpect(status().isUnauthorized());

        verify(companyPscService
                ,times(0)).deletePsc("123456789",MOCK_NOTIFICATION_ID);
    }

    @Test
    void callPscDeleteRequest() throws Exception {

        doNothing()
                .when(companyPscService).deletePsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

        mockMvc.perform(delete(DELETE_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isOk());

        verify(companyPscService,times(1)).deletePsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);
    }

    @Test
    void callPscDeleteRequestAndReturn404() throws Exception {

        doThrow(ResourceNotFoundException.class)
                .when(companyPscService).deletePsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

        mockMvc.perform(delete(DELETE_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isNotFound());

        verify(companyPscService,times(1)).deletePsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);
    }

    @Test
    void callPscDeleteRequestWhenServiceUnavailableAndReturn503() throws Exception {

        doThrow(ServiceUnavailableException.class)
                .when(companyPscService).deletePsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

        mockMvc.perform(delete(DELETE_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isInternalServerError());

    }


    @Test
    @DisplayName("Return 401 when no api key is present")
    void getPSCWhenNoApiKeyPresent() throws Exception {
        mockMvc.perform(get(GET_URL)).andExpect(status().isUnauthorized());

        verify(companyPscService
                ,times(0)).getIndividualPsc( "123456789",MOCK_NOTIFICATION_ID);
    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when Individual PSC found")
    void getIndividualPSCFound() throws Exception {
        when(companyPscService.getIndividualPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID)).thenReturn(individual);

        mockMvc.perform(get(GET_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isOk());

        verify(companyPscService).getIndividualPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getIndividualPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService.getIndividualPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID)).thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isInternalServerError());

        verify(companyPscService).getIndividualPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getIndividualPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService.getIndividualPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isNotFound());

        verify(companyPscService).getIndividualPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }


    @Test
    @DisplayName("Return 401 when no api key is present")
    void getIndividualBeneficialOwnerPSCWhenNoApiKeyPresent() throws Exception {
        mockMvc.perform(get(GET_IndividualBeneficialOwner_URL)).andExpect(status().isUnauthorized());

        verify(companyPscService
                ,times(0))
                .getIndividualBeneficialOwnerPsc( "123456789",MOCK_NOTIFICATION_ID);
    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when IBO PSC found")
    void getIndividualBeneficialOwnerPSCFound() throws Exception {
        when(companyPscService
                .getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID))
                .thenReturn(individualBeneficialOwner);

        mockMvc.perform(get(GET_IndividualBeneficialOwner_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isOk());

        verify(companyPscService).getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getIndividualBeneficialOwnerPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService
                .getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID))
                .thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_IndividualBeneficialOwner_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isInternalServerError());

        verify(companyPscService).getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getIndividualBeneficialOwnerPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService
                .getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID))
                .thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_IndividualBeneficialOwner_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isNotFound());

        verify(companyPscService).getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when IBO PSC found")
    void getCorporateBeneficialOwnerPSCFound() throws Exception {
        when(companyPscService
                .getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID))
                .thenReturn(corporateEntityBeneficialOwner);

        mockMvc.perform(get(GET_CorporateEntityBeneficialOwner_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isOk());

        verify(companyPscService).getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getCorporateEntityBeneficialOwnerPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService
                .getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID))
                .thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_CorporateEntityBeneficialOwner_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isInternalServerError());

        verify(companyPscService).getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getCorporateEntityBeneficialOwnerPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService
                .getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID))
                .thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_CorporateEntityBeneficialOwner_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isNotFound());

        verify(companyPscService).getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when Legal Person PSC found")
    void getLegalPSCFound() throws Exception {
        when(companyPscService
                .getLegalPersonPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID))
                .thenReturn(legalPerson);

        mockMvc.perform(get(GET_Legal_Person_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isOk());

        verify(companyPscService).getLegalPersonPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getLegalPersonPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService
                .getLegalPersonPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID))
                .thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_Legal_Person_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isInternalServerError());

        verify(companyPscService).getLegalPersonPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getLegalPersonPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService
                .getLegalPersonPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID))
                .thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_Legal_Person_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isNotFound());

        verify(companyPscService).getLegalPersonPsc(MOCK_COMPANY_NUMBER,MOCK_NOTIFICATION_ID);

    }


}