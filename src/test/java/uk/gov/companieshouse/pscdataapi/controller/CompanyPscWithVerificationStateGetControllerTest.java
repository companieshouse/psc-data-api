package uk.gov.companieshouse.pscdataapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.common.Address;
import uk.gov.companieshouse.api.model.common.Date3Tuple;
import uk.gov.companieshouse.api.model.psc.NameElementsApi;
import uk.gov.companieshouse.api.model.psc.PscIndividualWithVerificationStateApi;
import uk.gov.companieshouse.api.model.psc.PscLinks;
import uk.gov.companieshouse.api.model.psc.VerificationState;
import uk.gov.companieshouse.api.model.psc.VerificationStatus;
import uk.gov.companieshouse.pscdataapi.exceptions.NotFoundException;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@SpringBootTest(properties = {"feature.identity_verification=true"})
@AutoConfigureMockMvc
class CompanyPscWithVerificationStateGetControllerTest {

    private static final String X_REQUEST_ID = "123456";
    private static final String MOCK_COMPANY_NUMBER = "1234567";
    private static final String MOCK_NOTIFICATION_ID = "123456789";
    private static final String ERIC_IDENTITY = "Test-Identity";
    private static final String ERIC_IDENTITY_TYPE = "key";
    private static final String ERIC_PRIVILEGES = "*";
    private static final String ERIC_AUTH_INTERNAL = "internal-app";

    private static final String GET_INDIVIDUAL_WITH_VERIFICATION_STATE_URL = String.format(
            "/company/%s/persons-with-significant-control/individual/%s/identity-verification-details", MOCK_COMPANY_NUMBER,
            MOCK_NOTIFICATION_ID);

    @MockitoBean
    private CompanyPscService companyPscService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompanyPscWithVerificationStateGetController companyPscController;

    @Test
    void contextLoads() {
        assertThat(companyPscController).isNotNull();
    }

    @Test
    @DisplayName("Should return 200 status with PSC data + verification state")
    void getIndividualPSC() throws Exception {
        when(companyPscService.getIndividualWithVerificationState(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenReturn(
                        createIndividualWithVerificationState());

        final String expectedData = """
                {
                  "kind": "individual-person-with-significant-control",
                  "date_of_birth": {
                    "month": 2,
                    "year": 2000
                  },
                  "name": "Andy Bob Smith",
                  "name_elements": {
                    "surname": "Smith",
                    "forename": "Andy",
                    "middle_name": "Bob"
                  },
                  "links": {
                      "self": "/company/123/persons-with-significant-control/456"
                  },
                  "nationality": "British",
                  "address": {
                    "address_line_1": "Home street",
                    "postal_code": "AB12 3CD",
                    "premises": "Cottage"
                  },
                  "natures_of_control": [
                    "nature of my control"
                  ],
                  "verification_state": {
                    "verification_status": "VERIFIED",
                    "verification_start_date": "2025-01-10",
                    "verification_statement_due_date": "2025-02-05"
                  }
                }
                """;

        mockMvc.perform(get(GET_INDIVIDUAL_WITH_VERIFICATION_STATE_URL).header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_INTERNAL)).andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(expectedData, true));
    }

    @Test
    @DisplayName("Should return 404 when Individual PSC not found")
    void shouldReturn404WhenIndividualPscNotFound() throws Exception {
        when(companyPscService.getIndividualWithVerificationState(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenThrow(new NotFoundException("Individual PSC document not found in Mongo with id " + MOCK_NOTIFICATION_ID));

        mockMvc.perform(get(GET_INDIVIDUAL_WITH_VERIFICATION_STATE_URL).header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_INTERNAL))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private static PscIndividualWithVerificationStateApi createIndividualWithVerificationState() {
        final Address serviceAddress = new Address();
        serviceAddress.setAddressLine1("addressLine1");
        serviceAddress.setPostalCode("CF12 3AB");
        serviceAddress.setPremises("1");

        final Address residentialAddress = new Address();
        residentialAddress.setPremises("Cottage");
        residentialAddress.setAddressLine1("Home street");
        residentialAddress.setPostalCode("AB12 3CD");

        final NameElementsApi nameElementsApi = new NameElementsApi();
        nameElementsApi.setForename("Andy");
        nameElementsApi.setMiddleName("Bob");
        nameElementsApi.setSurname("Smith");

        final PscLinks pscLinks = new PscLinks();
        pscLinks.setSelf("/company/123/persons-with-significant-control/456");

        return new PscIndividualWithVerificationStateApi()
                .kind(PscIndividualWithVerificationStateApi.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL)
                .name("Andy Bob Smith")
                .nameElements(nameElementsApi)
                .address(residentialAddress)
                .nationality("British")
                .naturesOfControl(Arrays.asList("nature of my control"))
                .dateOfBirth(new Date3Tuple(0, 2, 2000))
                .links(pscLinks)
                .verificationState(new VerificationState(VerificationStatus.VERIFIED, LocalDate.of(2025, 1, 10),
                        LocalDate.of(2025, 2, 5)));
    }

}
