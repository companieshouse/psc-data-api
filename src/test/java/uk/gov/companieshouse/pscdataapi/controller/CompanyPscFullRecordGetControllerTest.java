package uk.gov.companieshouse.pscdataapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.InternalData;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@SpringBootTest(properties = {"feature.psc_individual_full_record_get=true"})
@AutoConfigureMockMvc
class CompanyPscFullRecordGetControllerTest {

    private static final String X_REQUEST_ID = "123456";
    private static final String MOCK_COMPANY_NUMBER = "1234567";
    private static final String MOCK_NOTIFICATION_ID = "123456789";
    private static final String ERIC_IDENTITY = "Test-Identity";
    private static final String ERIC_IDENTITY_TYPE = "key";
    private static final String ERIC_PRIVILEGES = "*";
    private static final String ERIC_AUTH_INTERNAL = "internal-app";

    private static final String GET_INDIVIDUAL_FULL_RECORD_URL = String.format(
        "/private/company/%s/persons-with-significant-control/individual/%s/full_record", MOCK_COMPANY_NUMBER,
        MOCK_NOTIFICATION_ID);
    private static final Instant FIXED_NOW = Instant.parse("2024-11-12T13:14:15Z");

    @MockBean
    private CompanyPscService companyPscService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompanyPscFullRecordGetController companyPscController;

    @Test
    void contextLoads() {
        assertThat(companyPscController).isNotNull();
    }

    @Test
    @DisplayName("Should return 200 status with full record data")
    void getIndividualPSC() throws Exception {
        when(companyPscService.getFullRecordPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenReturn(
            createFullRecord());

        mockMvc.perform(get(GET_INDIVIDUAL_FULL_RECORD_URL).header("ERIC-Identity", ERIC_IDENTITY)
            .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
            .contentType(APPLICATION_JSON)
            .header("x-request-id", X_REQUEST_ID)
            .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_INTERNAL)).andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.internal_data.delta_at").value("2024-11-12T13:14:15Z"));
        // TODO: expect more (dummy) data in response
    }

    private static @NotNull FullRecordCompanyPSCApi createFullRecord() {
        return new FullRecordCompanyPSCApi().internalData(
            new InternalData(OffsetDateTime.ofInstant(FIXED_NOW, ZoneId.of("UTC"))))
            .externalData(new ExternalData());
    }

}
