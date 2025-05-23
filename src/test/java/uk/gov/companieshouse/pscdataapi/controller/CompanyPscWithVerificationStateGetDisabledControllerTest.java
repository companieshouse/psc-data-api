package uk.gov.companieshouse.pscdataapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@SpringBootTest(properties = {"feature.identity_verification=false"}, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CompanyPscWithVerificationStateGetDisabledControllerTest {

    private static final String X_REQUEST_ID = "123456";
    private static final String MOCK_COMPANY_NUMBER = "1234567";
    private static final String MOCK_NOTIFICATION_ID = "123456789";
    private static final Boolean MOCK_REGISTER_VIEW_FALSE = false;
    private static final String ERIC_IDENTITY = "Test-Identity";
    private static final String ERIC_IDENTITY_TYPE = "key";
    private static final String ERIC_PRIVILEGES = "*";
    private static final String ERIC_AUTH = "internal-app";

    private static final String GET_INDIVIDUAL_WITH_VERIFICATION_STATE_URL = String.format(
            "/private/company/%s/persons-with-significant-control/individual/%s/verification-state", MOCK_COMPANY_NUMBER,
            MOCK_NOTIFICATION_ID);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private CompanyPscService companyPscService;

    @Nested
    @DisplayName("When feature flag not enabled")
    class FeatureFlagNotEnabled {

        @Test
        @DisplayName("should not create Controller bean")
        void shouldNotCreateWithVerificationStateGetControllerBean() {
            final var exception = assertThrows(NoSuchBeanDefinitionException.class,
                    () -> context.getBean(CompanyPscWithVerificationStateGetController.class));

            assertThat(exception.getBeanType()).isEqualTo(CompanyPscWithVerificationStateGetController.class);
        }

        @Test
        @DisplayName("should return 404 status for GET request")
        void shouldReturn404NotFound() throws Exception {
            when(companyPscService.getIndividualPsc(MOCK_COMPANY_NUMBER, MOCK_NOTIFICATION_ID,
                    MOCK_REGISTER_VIEW_FALSE)).thenReturn(new Individual());

            mockMvc.perform(get(GET_INDIVIDUAL_WITH_VERIFICATION_STATE_URL).header("ERIC-Identity", ERIC_IDENTITY)
                    .header("ERIC-Identity-Type", ERIC_IDENTITY_TYPE)
                    .contentType(APPLICATION_JSON)
                    .header("x-request-id", X_REQUEST_ID)
                    .header("ERIC-Authorised-Key-Roles", ERIC_PRIVILEGES)
                    .header("ERIC-Authorised-Key-Privileges", ERIC_AUTH)).andExpect(status().isNotFound());

        }
    }

}
