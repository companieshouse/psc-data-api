package uk.gov.companieshouse.pscdataapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyPscControllerTest {
    private static final String PUT_URL =
            "/company/123456789/persons-with-significant-control/123456789/full_record";
    private static final String X_REQUEST_ID = "123456";

    private static final String MOCK_COMPANY_NUMBER = "123456789";

    private static final String ERIC_IDENTITY = "Test-Identity";
    private static final String ERIC_IDENTITY_TYPE = "key";
    private static final String ERIC_PRIVILEGES = "*";
    //private static final String X_REQUEST_ID = TestHelper.X_REQUEST_ID;

    private static final String DELETE_URL = String.format("/company/%s/persons-with-significant-control/%s/full_record", MOCK_COMPANY_NUMBER, MOCK_COMPANY_NUMBER);


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
                .header("ERIC-Identity", "test")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Roles", "*")
                .content(TestHelper.createJsonPayload()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Return 401 when no api key is present")
    void deleteCompanyProfileWhenNoApiKeyPresent() throws Exception {
        mockMvc.perform(delete(PUT_URL)).andExpect(status().isUnauthorized());

        verify(companyPscService
                ,times(0)).deletePsc("123456789");
    }

    @Test
    void callPscStatementDeleteRequest() throws Exception {

        doNothing()
                .when(companyPscService).deletePsc(MOCK_COMPANY_NUMBER);

        mockMvc.perform(delete(DELETE_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isOk());

        verify(companyPscService,times(1)).deletePsc(MOCK_COMPANY_NUMBER);
    }

    @Test
    void callPscStatementDeleteRequestAndReturn404() throws Exception {

        doThrow(ResourceNotFoundException.class)
                .when(companyPscService).deletePsc(MOCK_COMPANY_NUMBER);

        mockMvc.perform(delete(DELETE_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isNotFound());

        verify(companyPscService,times(1)).deletePsc(MOCK_COMPANY_NUMBER);
    }

    @Test
    void callPscStatementDeleteRequestWhenServiceUnavailableAndReturn503() throws Exception {

        doThrow(ServiceUnavailableException.class)
                .when(companyPscService).deletePsc(MOCK_COMPANY_NUMBER);

        mockMvc.perform(delete(DELETE_URL)
                        .header("ERIC-Identity", "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "123456")
                        .header("ERIC-Authorised-Key-Roles", "*")
                        .header("ERIC-Authorised-Key-Privileges", "internal-app"))
                .andExpect(status().isInternalServerError());

        verify(companyPscService,times(1)).deletePsc(MOCK_COMPANY_NUMBER);
    }




}