package uk.gov.companieshouse.pscdataapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyPscControllerTest {
    private static final String PUT_URL =
            "/company/123456789/persons-with-significant-control/123456789/full_record";
    private static final String X_REQUEST_ID = "123456";

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
                .content(TestHelper.createJsonPayload()))
                .andExpect(status().isCreated());
    }
}