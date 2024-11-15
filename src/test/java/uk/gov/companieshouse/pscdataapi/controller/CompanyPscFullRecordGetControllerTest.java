package uk.gov.companieshouse.pscdataapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.psc.*;
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
        when(companyPscService.getIndividualFullRecord(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID)).thenReturn(
            createFullRecord());

        mockMvc.perform(get(GET_INDIVIDUAL_FULL_RECORD_URL).header("ERIC-Identity", ERIC_IDENTITY)
            .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
            .contentType(APPLICATION_JSON)
            .header("x-request-id", X_REQUEST_ID)
            .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
            .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH_INTERNAL)).andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.kind").value("individual-person-with-significant-control"))
            .andExpect(jsonPath("$.name").value("Andy Bob Smith"))
            .andExpect(jsonPath("$.name_elements.forename").value("Andy"))
            .andExpect(jsonPath("$.name_elements.middle_name").value("Bob"))
            .andExpect(jsonPath("$.name_elements.surname").value("Smith"))
            .andExpect(jsonPath("$.address.address_line_1").value("addressLine1"))
            .andExpect(jsonPath("$.address.postal_code").value("CF12 3AB"))
            .andExpect(jsonPath("$.address.premises").value("1"))
            .andExpect(jsonPath("$.residential_address_same_as_service_address").value(Boolean.FALSE))
            .andExpect(jsonPath("$.usual_residential_address.address_line_1").value("Home street"))
            .andExpect(jsonPath("$.usual_residential_address.postal_code").value("AB12 3CD"))
            .andExpect(jsonPath("$.usual_residential_address.premise").value("Cottage"))
            .andExpect(jsonPath("$.nationality").value("British"))
            .andExpect(jsonPath("$.natures_of_control[0]").value("nature of my control"))
            .andExpect(jsonPath("$.date_of_birth.day").value(1))
            .andExpect(jsonPath("$.date_of_birth.month").value(2))
            .andExpect(jsonPath("$.date_of_birth.year").value(2000))
            .andExpect(jsonPath("$.links[0].self").value("/company/123/persons-with-significant-control/456")) ;
    }

    private static @NotNull IndividualFullRecord createFullRecord() {
        return new IndividualFullRecord()
                .kind(IndividualFullRecord.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL)
                .name("Andy Bob Smith")
                .nameElements(new NameElements().forename("Andy").middleName("Bob").surname("Smith"))
                .address(new Address("addressLine1", "CF12 3AB", "1"))
                .residentialAddressSameAsServiceAddress(Boolean.FALSE)
                .usualResidentialAddress(new UsualResidentialAddress().premise("Cottage").addressLine1("Home street").postalCode("AB12 3CD"))
                .nationality("British")
                .naturesOfControl(Arrays.asList("nature of my control"))
                .dateOfBirth(new DateOfBirth().day(1).month(2).year(2000))
                .links(Arrays.asList(new ItemLinkTypes().self("/company/123/persons-with-significant-control/456")));
    }

}
