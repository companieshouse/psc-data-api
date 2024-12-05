package uk.gov.companieshouse.pscdataapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.companieshouse.api.psc.CorporateEntity;
import uk.gov.companieshouse.api.psc.CorporateEntityBeneficialOwner;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.psc.IndividualBeneficialOwner;
import uk.gov.companieshouse.api.psc.LegalPerson;
import uk.gov.companieshouse.api.psc.LegalPersonBeneficialOwner;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.api.psc.SuperSecure;
import uk.gov.companieshouse.api.psc.SuperSecureBeneficialOwner;
import uk.gov.companieshouse.pscdataapi.exceptions.ResourceNotFoundException;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.models.PscDeleteRequest;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyPscControllerTest {

    private static final String X_REQUEST_ID = "123456";
    private static final String MOCK_COMPANY_NUMBER = "1234567";
    private static final String MOCK_NOTIFICATION_ID = "123456789";
    private static final String KIND = "individual-person-with-significant-control";
    private static final String DELTA_AT = "20240219123045999999";
    private static final Boolean MOCK_REGISTER_VIEW_TRUE = true;
    private static final Boolean MOCK_REGISTER_VIEW_FALSE = false;
    private static final String ERIC_IDENTITY = "Test-Identity";
    private static final String ERIC_IDENTITY_TYPE = "key";
    private static final String ERIC_PRIVILEGES = "*";
    private static final String ERIC_AUTH = "internal-app";

    private static final String PUT_URL = String.format(
            "/company/%s/persons-with-significant-control/%s/full_record", MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    private static final String GET_Individual_URL = String.format(
            "/company/%s/persons-with-significant-control/individual/%s", MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    private static final String GET_IndividualBeneficialOwner_URL = String.format(
            "/company/%s/persons-with-significant-control/individual-beneficial-owner/%s", MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    private static final String GET_CorporateEntity_URL = String.format(
            "/company/%s/persons-with-significant-control/corporate-entity/%s", MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    private static final String GET_CorporateEntityBeneficialOwner_URL = String.format(
            "/company/%s/persons-with-significant-control/corporate-entity-beneficial-owner/%s", MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    private static final String GET_Legal_Person_URL = String.format(
            "/company/%s/persons-with-significant-control/legal-person/%s", MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    private static final String GET_Legal_Person_Beneficial_Owner_URL = String.format(
            "/company/%s/persons-with-significant-control/legal-person-beneficial-owner/%s", MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    private static final String GET_SuperSecure_URL = String.format(
            "/company/%s/persons-with-significant-control/super-secure/%s", MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    private static final String GET_SuperSecureBeneficialOwner_URL = String.format(
            "/company/%s/persons-with-significant-control/super-secure-beneficial-owner/%s", MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    private static final String GET_List_Summary_URL = String.format(
            "/company/%s/persons-with-significant-control", MOCK_COMPANY_NUMBER);
    private static final String DELETE_URL = String.format(
            "/company/%s/persons-with-significant-control/%s/full_record", MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    @MockBean
    private CompanyPscService companyPscService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompanyPscController companyPscController;

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
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .content(TestHelper.createJsonPayload()))
                .andExpect(status().isCreated());
    }

    @Test
    void callPutRequestNoPrivileges() throws Exception {
        doNothing()
                .when(companyPscService).insertPscRecord(anyString(), isA(FullRecordCompanyPSCApi.class));

        mockMvc.perform(put(PUT_URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .content(TestHelper.createJsonPayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Return 401 for Super Secure when no api key is present")
    void getSuperSecurePSCWhenNoApiKeyPresent() throws Exception {
        mockMvc.perform(get(GET_SuperSecure_URL)).andExpect(status().isUnauthorized());

        verify(companyPscService
                , times(0)).getSuperSecurePsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when Super Secure PSC found")
    void getSuperSecurePSCFound() throws Exception {
        when(companyPscService.getSuperSecurePsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenReturn(new SuperSecure());

        mockMvc.perform(get(GET_SuperSecure_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isOk());

        verify(companyPscService).getSuperSecurePsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getSuperSecurePSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService.getSuperSecurePsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_SuperSecure_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isServiceUnavailable());

        verify(companyPscService).getSuperSecurePsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getSuperSecurePSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService.getSuperSecurePsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_SuperSecure_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isNotFound());

        verify(companyPscService).getSuperSecurePsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName("Return 401 for Super Secure Beneficial Owner when no api key is present")
    void getSuperSecureBeneficialOwnerPSCWhenNoApiKeyPresent() throws Exception {
        mockMvc.perform(get(GET_SuperSecureBeneficialOwner_URL)).andExpect(status().isUnauthorized());

        verify(companyPscService
                , times(0)).getSuperSecureBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when Super Secure Beneficial Owner PSC found")
    void getSuperSecureBeneficialOwnerPSCFound() throws Exception {
        when(companyPscService.getSuperSecureBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenReturn(new SuperSecureBeneficialOwner());

        mockMvc.perform(get(GET_SuperSecureBeneficialOwner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isOk());

        verify(companyPscService).getSuperSecureBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getSuperSecureBeneficialOwnerPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService.getSuperSecureBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_SuperSecureBeneficialOwner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isServiceUnavailable());

        verify(companyPscService).getSuperSecureBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getSuperSecureBeneficialOwnerPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService.getSuperSecureBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_SuperSecureBeneficialOwner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isNotFound());

        verify(companyPscService).getSuperSecureBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName("Return 401 for Corporate Entity when no api key is present")
    void getCorporateEntityPSCWhenNoApiKeyPresent() throws Exception {
        mockMvc.perform(get(GET_CorporateEntity_URL)).andExpect(status().isUnauthorized());

        verify(companyPscService
                , times(0)).getCorporateEntityPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);
    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when Corporate Entity PSC found")
    void getCorporateEntityPSCFound() throws Exception {
        when(companyPscService.getCorporateEntityPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenReturn(new CorporateEntity());

        mockMvc.perform(get(GET_CorporateEntity_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isOk());

        verify(companyPscService).getCorporateEntityPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when Corporate Entity PSC found")
    void getCorporateEntityPSCWithoutPrivileges() throws Exception {
        when(companyPscService.getCorporateEntityPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenReturn(new CorporateEntity());

        mockMvc.perform(get(GET_CorporateEntity_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", "")
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isOk());

        verify(companyPscService).getCorporateEntityPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getCorporateEntityPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService.getCorporateEntityPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_CorporateEntity_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isServiceUnavailable());

        verify(companyPscService).getCorporateEntityPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getCorporateEntityPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService.getCorporateEntityPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_CorporateEntity_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isNotFound());

        verify(companyPscService).getCorporateEntityPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName("Return 401 when no api key is present")
    void deletePSCWhenNoApiKeyPresent() throws Exception {
        mockMvc.perform(delete(PUT_URL)).andExpect(status().isUnauthorized());

        verify(companyPscService
                , times(0)).deletePsc(new PscDeleteRequest(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, "", KIND, DELTA_AT));
    }

    @Test
    void callPscDeleteRequest() throws Exception {
        PscDeleteRequest deleteRequest = new PscDeleteRequest(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, X_REQUEST_ID, KIND, DELTA_AT);

        doNothing()
                .when(companyPscService).deletePsc(deleteRequest);

        mockMvc.perform(delete(DELETE_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH)
                        .header("x-kind", KIND)
                        .header("x-delta-at", DELTA_AT))
                .andExpect(status().isOk());

        verify(companyPscService, times(1)).deletePsc(deleteRequest);
    }

    @Test
    void callPscDeleteRequestAndReturn404() throws Exception {
        PscDeleteRequest deleteRequest = new PscDeleteRequest(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, X_REQUEST_ID, KIND, DELTA_AT);

        doThrow(ResourceNotFoundException.class)
                .when(companyPscService).deletePsc(deleteRequest);

        mockMvc.perform(delete(DELETE_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH)
                        .header("x-kind", KIND)
                        .header("x-delta-at", DELTA_AT))
                .andExpect(status().isNotFound());

        verify(companyPscService, times(1)).deletePsc(deleteRequest);
    }

    @Test
    void callPscDeleteRequestWhenServiceUnavailableAndReturn503() throws Exception {
        PscDeleteRequest deleteRequest = new PscDeleteRequest(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, X_REQUEST_ID, KIND, DELTA_AT);

        doThrow(ServiceUnavailableException.class)
                .when(companyPscService).deletePsc(deleteRequest);

        mockMvc.perform(delete(DELETE_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isInternalServerError());

    }


    @Test
    @DisplayName("Return 401 when no api key is present")
    void getPSCWhenNoApiKeyPresent() throws Exception {
        mockMvc.perform(get(GET_Individual_URL)).andExpect(status().isUnauthorized());

        verify(companyPscService, times(0))
                .getIndividualPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_TRUE);
    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when Individual PSC found")
    void getIndividualPSCFound() throws Exception {
        when(companyPscService.getIndividualPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_FALSE)).thenReturn(new Individual());

        mockMvc.perform(get(GET_Individual_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isOk());

        verify(companyPscService).getIndividualPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_FALSE);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getIndividualPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService.getIndividualPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_TRUE)).thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_Individual_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .param("register_view", "true")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isServiceUnavailable());

        verify(companyPscService).getIndividualPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_TRUE);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getIndividualPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService.getIndividualPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_TRUE)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_Individual_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .param("register_view", "true")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isNotFound());

        verify(companyPscService).getIndividualPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_TRUE);

    }


    @Test
    @DisplayName("Return 401 when no api key is present")
    void getIndividualBeneficialOwnerPSCWhenNoApiKeyPresent() throws Exception {
        mockMvc.perform(get(GET_IndividualBeneficialOwner_URL)).andExpect(status().isUnauthorized());

        verify(companyPscService
                , times(0))
                .getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_FALSE);
    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when IBO PSC found")
    void getIndividualBeneficialOwnerPSCFound() throws Exception {
        when(companyPscService
                .getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_FALSE))
                .thenReturn(new IndividualBeneficialOwner());

        mockMvc.perform(get(GET_IndividualBeneficialOwner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .param("register_view", "true")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isOk());

        verify(companyPscService).getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_TRUE);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getIndividualBeneficialOwnerPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService
                .getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_FALSE))
                .thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_IndividualBeneficialOwner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isServiceUnavailable());

        verify(companyPscService).getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_FALSE);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getIndividualBeneficialOwnerPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService
                .getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_FALSE))
                .thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_IndividualBeneficialOwner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isNotFound());

        verify(companyPscService).getIndividualBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID, MOCK_REGISTER_VIEW_FALSE);

    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when IBO PSC found")
    void getCorporateBeneficialOwnerPSCFound() throws Exception {
        when(companyPscService
                .getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenReturn(new CorporateEntityBeneficialOwner());

        mockMvc.perform(get(GET_CorporateEntityBeneficialOwner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isOk());

        verify(companyPscService).getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getCorporateEntityBeneficialOwnerPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService
                .getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_CorporateEntityBeneficialOwner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isServiceUnavailable());

        verify(companyPscService).getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getCorporateEntityBeneficialOwnerPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService
                .getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_CorporateEntityBeneficialOwner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isNotFound());

        verify(companyPscService).getCorporateEntityBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 200 response when Legal Person PSC found")
    void getLegalPSCFound() throws Exception {
        when(companyPscService
                .getLegalPersonPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenReturn(new LegalPerson());

        mockMvc.perform(get(GET_Legal_Person_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isOk());

        verify(companyPscService).getLegalPersonPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getLegalPersonPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService
                .getLegalPersonPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_Legal_Person_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isServiceUnavailable());

        verify(companyPscService).getLegalPersonPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getLegalPersonPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService
                .getLegalPersonPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_Legal_Person_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isNotFound());

        verify(companyPscService).getLegalPersonPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName("GET request returns a 200 response when Legal Person PSC found")
    void getLegalPersonBeneficialOwnerPSCFound() throws Exception {
        when(companyPscService
                .getLegalPersonBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenReturn(new LegalPersonBeneficialOwner());

        mockMvc.perform(get(GET_Legal_Person_Beneficial_Owner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isOk());

        verify(companyPscService).getLegalPersonBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 503 response when service is unavailable")
    void getLegalPersonBeneficialOwnerPSCDocumentWhenServiceIsDown() throws Exception {
        when(companyPscService
                .getLegalPersonBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get(GET_Legal_Person_Beneficial_Owner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isServiceUnavailable());

        verify(companyPscService).getLegalPersonBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    @DisplayName(
            "GET request returns a 404 response when Resource is not found")
    void getLegalPersonBeneficialOwnerPSCDocumentWhenResourceNotFound() throws Exception {
        when(companyPscService
                .getLegalPersonBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(GET_Legal_Person_Beneficial_Owner_URL)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
                .andExpect(status().isNotFound());

        verify(companyPscService).getLegalPersonBeneficialOwnerPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID);

    }

    @Test
    void callPscListGetRequestWithParams() throws Exception {
        when(companyPscService.retrievePscListSummaryFromDb(MOCK_COMPANY_NUMBER, 2, false, 5))
                .thenReturn(new PscList());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_List_Summary_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH)
                        .header("items_per_page", 5)
                        .header("start_index", 2))
                .andExpect(status().isOk());
    }

    @Test
    void callPscListGetRequestWithTrailingSlash() throws Exception {
        when(companyPscService.retrievePscListSummaryFromDb(MOCK_COMPANY_NUMBER, 2, false, 5))
                .thenReturn(new PscList());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_List_Summary_URL+"/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH)
                        .header("items_per_page", 5)
                        .header("start_index", 2))
                .andExpect(status().isOk());
    }

    @Test
    void callPscListGetRequestWithRegisterView() throws Exception {
        when(companyPscService.retrievePscListSummaryFromDb(MOCK_COMPANY_NUMBER, 2, true, 5))
                .thenReturn(new PscList());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_List_Summary_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        //.contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH)
                        .header("items_per_page", 5)
                        .header("start_index", 2))
                .andExpect(status().isOk());
    }

    @Test
    void callPscListGetRequestNoParams() throws Exception {
        when(companyPscService.retrievePscListSummaryFromDb(MOCK_COMPANY_NUMBER, 0, false, 25))
                .thenReturn(new PscList());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_List_Summary_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH)
                        .header("ERIC-IDENTITY", ERIC_IDENTITY)
                        .header("ERIC-IDENTITY-TYPE", ERIC_IDENTITY_TYPE))
                .andExpect(status().isOk());
    }

    @Test
    void callPscListOptionsRequestWithParamsCORS() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .options(GET_List_Summary_URL)
                        .header("Origin", "")
                        .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }

    @Test
    void callPscListGetRequestWithParamsCORS() throws Exception {
        when(companyPscService.retrievePscListSummaryFromDb(MOCK_COMPANY_NUMBER, 2, false, 5))
                .thenReturn(new PscList());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_List_Summary_URL)
                        .header("Origin", "")
                        .header("ERIC-Allowed-Origin", "some-origin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH)
                        .header("items_per_page", 5)
                        .header("start_index", 2))
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")));
    }

    @Test
    void callPscListGetRequestWithParamsForbiddenCORS() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_List_Summary_URL)
                        .header("Origin", "")
                        .header("ERIC-Allowed-Origin", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH)
                        .header("items_per_page", 5)
                        .header("start_index", 2))
            .andExpect(status().isForbidden())
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
            .andExpect(content().string(""));
    }

    @Test
    void callPscDeleteRequestForbiddenCORS() throws Exception {

        mockMvc.perform(delete(DELETE_URL)
                        .header("Origin", "")
                        .header("ERIC-Allowed-Origin", "some-origin")
                        .header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH))
            .andExpect(status().isForbidden())
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
            .andExpect(content().string(""));
    }

}
