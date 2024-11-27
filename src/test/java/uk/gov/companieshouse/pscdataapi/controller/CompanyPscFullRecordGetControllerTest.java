package uk.gov.companieshouse.pscdataapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.psc.Address;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.api.psc.NameElements;
import uk.gov.companieshouse.pscdataapi.exceptions.ResourceNotFoundException;
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
        "/company/%s/persons-with-significant-control/individual/%s/full_record", MOCK_COMPANY_NUMBER,
        MOCK_NOTIFICATION_ID);

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
              "links": [
                {
                  "self": "/company/123/persons-with-significant-control/456"
                }
              ],
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
              "residential_address_same_as_service_address": false
            }
            """;

        mockMvc.perform(get(GET_INDIVIDUAL_FULL_RECORD_URL).header("ERIC-Identity", ERIC_IDENTITY)
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
        when(companyPscService.getIndividualFullRecord(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenThrow(new ResourceNotFoundException(
            HttpStatus.NOT_FOUND,
            "Individual PSC document not found in Mongo with id " + MOCK_NOTIFICATION_ID));

        mockMvc.perform(get(GET_INDIVIDUAL_FULL_RECORD_URL).header("ERIC-Identity", ERIC_IDENTITY)
                .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                .contentType(APPLICATION_JSON)
                .header("x-request-id", X_REQUEST_ID)
                .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_INTERNAL))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    private static IndividualFullRecord createFullRecord() {
        return new IndividualFullRecord()
                .kind(IndividualFullRecord.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL)
                .name("Andy Bob Smith")
                .nameElements(new NameElements().forename("Andy").middleName("Bob").surname("Smith"))
                .serviceAddress(new Address().addressLine1("addressLine1").postalCode("CF12 3AB").premises("1"))
                .residentialAddressSameAsServiceAddress(Boolean.FALSE)
                .usualResidentialAddress(new Address().premises("Cottage").addressLine1("Home street").postalCode("AB12 3CD"))
                .nationality("British")
                .naturesOfControl(Arrays.asList("nature of my control"))
                .dateOfBirth(new DateOfBirth().day(1).month(2).year(2000))
                .links(Arrays.asList(new ItemLinkTypes().self("/company/123/persons-with-significant-control/456")));
    }

}
