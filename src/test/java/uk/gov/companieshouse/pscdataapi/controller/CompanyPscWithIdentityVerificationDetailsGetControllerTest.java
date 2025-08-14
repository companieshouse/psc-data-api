package uk.gov.companieshouse.pscdataapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.common.Address;
import uk.gov.companieshouse.api.model.common.Date3Tuple;
import uk.gov.companieshouse.api.model.psc.IdentityVerificationDetails;
import uk.gov.companieshouse.api.model.psc.NameElementsApi;
import uk.gov.companieshouse.api.model.psc.PscIndividualWithIdentityVerificationDetailsApi;
import uk.gov.companieshouse.api.model.psc.PscLinks;
import uk.gov.companieshouse.pscdataapi.exceptions.InternalDataException;
import uk.gov.companieshouse.pscdataapi.exceptions.NotFoundException;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@SpringBootTest(properties = {"feature.identity_verification=true"})
@AutoConfigureMockMvc
class CompanyPscWithIdentityVerificationDetailsGetControllerTest {

    private static final String X_REQUEST_ID = "123456";
    private static final String MOCK_COMPANY_NUMBER = "1234567";
    private static final String MOCK_NOTIFICATION_ID = "123456789";
    private static final String ERIC_IDENTITY = "Test-Identity";
    private static final String ERIC_IDENTITY_TYPE = "key";
    private static final String ERIC_PRIVILEGES = "*";
    private static final String ERIC_AUTH_INTERNAL = "internal-app";
    private static final LocalDate START_ON = LocalDate.parse("2025-06-12");
    private static final LocalDate END_ON = LocalDate.parse("9999-12-31");
    private static final LocalDate STATEMENT_DATE = LocalDate.parse("2025-06-01");
    private static final LocalDate STATEMENT_DUE_DATE = LocalDate.parse("2025-06-15");

    private static final String GET_INDIVIDUAL_WITH_IDENTITY_VERIFICATION_DETAILS_URL = String.format(
            "/company/%s/persons-with-significant-control/individual/%s/identity-verification-details", MOCK_COMPANY_NUMBER,
            MOCK_NOTIFICATION_ID);

    @MockitoBean
    private CompanyPscService companyPscService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CompanyPscWithIdentityVerificationDetailsGetController companyPscController;

    @Test
    void contextLoads() {
        assertThat(companyPscController).isNotNull();
    }

    @Test
    @DisplayName("Should return 200 status with PSC data + identity verification details")
    void getIndividualPSC() throws Exception {
        when(companyPscService.getIndividualWithIdentityVerificationDetails(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenReturn(createIndividualWithIdentityVerificationDetails());

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
                  "identity_verification_details": {
                    "appointment_verification_start_on": "%s",
                    "appointment_verification_end_on": "%s",
                    "appointment_verification_statement_date": "%s",
                    "appointment_verification_statement_due_on": "%s"
                  }
                }
                """.formatted(START_ON, END_ON, STATEMENT_DATE, STATEMENT_DUE_DATE);

        mockMvc.perform(get(GET_INDIVIDUAL_WITH_IDENTITY_VERIFICATION_DETAILS_URL).header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_INTERNAL)).andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(expectedData, JsonCompareMode.STRICT));
    }

    @Test
    @DisplayName("Should return 404 when Individual PSC not found")
    void shouldReturn404WhenIndividualPscNotFound() throws Exception {
        when(companyPscService.getIndividualWithIdentityVerificationDetails(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenThrow(new NotFoundException("Individual PSC document not found in Mongo with id " + MOCK_NOTIFICATION_ID));

        mockMvc.perform(get(GET_INDIVIDUAL_WITH_IDENTITY_VERIFICATION_DETAILS_URL).header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_INTERNAL))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 500 when Individual PSC record has no internal ID")
    void shouldReturn500WhenIndividualPscRecordHasNoInternalId() throws Exception {
        when(companyPscService.getIndividualWithIdentityVerificationDetails(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID))
                .thenThrow(new InternalDataException("Individual PSC document found with no internal_id " + MOCK_NOTIFICATION_ID));

        mockMvc.perform(get(GET_INDIVIDUAL_WITH_IDENTITY_VERIFICATION_DETAILS_URL).header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_INTERNAL))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    private static PscIndividualWithIdentityVerificationDetailsApi createIndividualWithIdentityVerificationDetails() {
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

        return new PscIndividualWithIdentityVerificationDetailsApi().kind(
                PscIndividualWithIdentityVerificationDetailsApi.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL)
                .name("Andy Bob Smith")
                .nameElements(nameElementsApi)
                .address(residentialAddress)
                .nationality("British")
                .naturesOfControl(List.of("nature of my control"))
                .dateOfBirth(new Date3Tuple(0, 2, 2000))
                .links(pscLinks)
                .identityVerificationDetails(
                new IdentityVerificationDetails(START_ON, END_ON, STATEMENT_DATE, STATEMENT_DUE_DATE));
    }

}
