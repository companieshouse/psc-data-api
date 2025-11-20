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
import uk.gov.companieshouse.api.model.common.Date3Tuple;
import uk.gov.companieshouse.api.model.psc.NameElementsApi;
import uk.gov.companieshouse.api.model.psc.PscLinks;
import uk.gov.companieshouse.api.psc.Address;
import uk.gov.companieshouse.api.psc.IdentityVerificationDetails;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
import uk.gov.companieshouse.pscdataapi.exceptions.InternalDataException;
import uk.gov.companieshouse.pscdataapi.exceptions.NotFoundException;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyPscFullRecordGetControllerTest {

    private static final String X_REQUEST_ID = "123456";
    private static final String MOCK_COMPANY_NUMBER = "1234567";
    private static final String MOCK_NOTIFICATION_ID = "123456789";
    private static final String ERIC_IDENTITY = "Test-Identity";
    private static final String ERIC_IDENTITY_TYPE_API_KEY = "key";
    private static final String ERIC_PRIVILEGES = "*";
    private static final String ERIC_AUTH_SENSITIVE = "sensitive-data";
    private static final LocalDate START_ON = LocalDate.parse("2025-06-12");
    private static final LocalDate END_ON = LocalDate.parse("9999-12-31");
    private static final LocalDate STATEMENT_DATE = LocalDate.parse("2025-06-01");
    private static final LocalDate STATEMENT_DUE_DATE = LocalDate.parse("2025-06-15");

    private static final String GET_INDIVIDUAL_FULL_RECORD_URL = String.format(
            "/company/%s/persons-with-significant-control/individual/%s/full_record", MOCK_COMPANY_NUMBER,
            MOCK_NOTIFICATION_ID);

    @MockitoBean
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
    @DisplayName("Should return 200 status with full record data when eric_identity_type=key")
    void getIndividualPSCWithApiKey() throws Exception {
        when(companyPscService.getIndividualFullRecord(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenReturn(
                createFullRecord());

        final String expectedData = """
                {
                  "kind": "individual-person-with-significant-control",
                  "date_of_birth": {
                    "day": 1,
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
                  "service_address": {
                    "address_line_1": "addressLine1",
                    "postal_code": "CF12 3AB",
                    "premises": "1"
                  },
                  "natures_of_control": [
                    "nature of my control"
                  ],
                  "usual_residential_address": {
                    "address_line_1": "Home street",
                    "postal_code": "AB12 3CD",
                    "premises": "Cottage"
                  },
                  "residential_address_same_as_service_address": false,
                  "internal_id" : 123456789,
                  "identity_verification_details": {
                    "appointment_verification_start_on": "%s",
                    "appointment_verification_end_on": "%s",
                    "appointment_verification_statement_date": "%s",
                    "appointment_verification_statement_due_on": "%s"
                  }
                }
                """.formatted(START_ON, END_ON, STATEMENT_DATE, STATEMENT_DUE_DATE);

        mockMvc.perform(get(GET_INDIVIDUAL_FULL_RECORD_URL).header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE_API_KEY)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_SENSITIVE)).andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(expectedData, JsonCompareMode.STRICT));
    }

    @Test
    @DisplayName("Should return 404 when Individual PSC not found")
    void shouldReturn404WhenIndividualPscNotFound() throws Exception {
        when(companyPscService.getIndividualFullRecord(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenThrow(
                new NotFoundException("Individual PSC document not found in Mongo with id " + MOCK_NOTIFICATION_ID));

        mockMvc.perform(get(GET_INDIVIDUAL_FULL_RECORD_URL).header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE_API_KEY)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_SENSITIVE))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 500 when Individual PSC has no internal ID")
    void shouldReturn500WhenIndividualPscRecordHasNoInternalId() throws Exception {
        when(companyPscService.getIndividualFullRecord(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenThrow(
                new InternalDataException("Individual PSC document found with no internal_id " + MOCK_NOTIFICATION_ID));

        mockMvc.perform(get(GET_INDIVIDUAL_FULL_RECORD_URL).header("ERIC-Identity", ERIC_IDENTITY)
                        .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE_API_KEY)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", X_REQUEST_ID)
                        .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                        .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_SENSITIVE))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    private static IndividualFullRecord createFullRecord() {
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

        final IdentityVerificationDetails identityVerificationDetails = new IdentityVerificationDetails();
        identityVerificationDetails.setAppointmentVerificationStartOn(START_ON);
        identityVerificationDetails.setAppointmentVerificationEndOn(END_ON);
        identityVerificationDetails.setAppointmentVerificationStatementDate(STATEMENT_DATE);
        identityVerificationDetails.setAppointmentVerificationStatementDueOn(STATEMENT_DUE_DATE);

        return new IndividualFullRecord()
                .kind(IndividualFullRecord.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL)
                .name("Andy Bob Smith")
                .nameElements(nameElementsApi)
                .serviceAddress(serviceAddress)
                .residentialAddressSameAsServiceAddress(Boolean.FALSE)
                .usualResidentialAddress(residentialAddress)
                .nationality("British")
                .naturesOfControl(List.of("nature of my control"))
                .dateOfBirth(new Date3Tuple(1, 2, 2000))
                .internalId(123456789L)
                .links(pscLinks)
                .identityVerificationDetails(identityVerificationDetails);
    }

}
