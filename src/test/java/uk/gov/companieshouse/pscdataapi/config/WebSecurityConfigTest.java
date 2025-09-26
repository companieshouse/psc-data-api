package uk.gov.companieshouse.pscdataapi.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.pscdataapi.interceptor.AuthenticationHelperImpl.ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.pscdataapi.controller.CompanyPscFullRecordGetController;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {
        CompanyPscFullRecordGetController.class
}, properties = {"feature.identity_verification=true"})
class WebSecurityConfigTest {

    private static final String MOCK_COMPANY_NUMBER = "1234567";
    private static final String MOCK_NOTIFICATION_ID = "123456789";
    private static final String ERIC_IDENTITY = "Test-Identity";
    private static final String GET_INDIVIDUAL_FULL_RECORD_URL = String.format(
            "/company/%s/persons-with-significant-control/individual/%s/full_record", MOCK_COMPANY_NUMBER,
            MOCK_NOTIFICATION_ID);

    @MockitoBean
    private CompanyPscService companyPscService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("should forbid access with underprivileged API key")
    void shouldForbidAccessWithUnderprivilegedApiKey() throws Exception {
        final var headers = createHttpHeaders(false);

        mockMvc.perform(get(GET_INDIVIDUAL_FULL_RECORD_URL)
                        .headers(headers)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("should allow access with privileged API key")
    void shouldAllowAccessWithInternalUserApiKey() throws Exception {
        final var headers = createHttpHeaders(true);

        mockMvc.perform(get(GET_INDIVIDUAL_FULL_RECORD_URL)
                        .headers(headers)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    private HttpHeaders createHttpHeaders(final boolean hasInternalPrivilege) {
        final HttpHeaders headers = new HttpHeaders();

        headers.add(EricConstants.ERIC_IDENTITY, ERIC_IDENTITY);
        headers.add(EricConstants.ERIC_IDENTITY_TYPE, SecurityConstants.API_KEY_IDENTITY_TYPE);
        headers.add(EricConstants.ERIC_AUTHORISED_KEY_ROLES,
                hasInternalPrivilege ? SecurityConstants.INTERNAL_USER_ROLE : "any_other_role");
        headers.add(ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER,
                hasInternalPrivilege ? "sensitive-data" : "internal-app");

        return headers;
    }
}
