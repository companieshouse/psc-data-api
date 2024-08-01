package uk.gov.companieshouse.pscdataapi.logging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.companieshouse.pscdataapi.logging.DataMapHolder.UNINITIALISED;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.controller.CompanyPscController;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@AutoConfigureMockMvc
@WebMvcTest(value = CompanyPscController.class)
@TestPropertySource(properties = "logging.level.root=INFO")
class DataMapHolderIT {

    private static final String GET_REQUEST_URI = "/company/{company_number}/persons-with-significant-control";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String CONTEXT_ID = "context_id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyPscService companyPscService;
    @MockBean
    private Logger logger;

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldSuccessfullyInitialiseRequestId(CapturedOutput capture) throws Exception {
        // given
        when(companyPscService.retrievePscListSummaryFromDb(anyString(), anyInt(), anyBoolean(), anyInt()))
                .thenReturn(new PscList());

        // when
        mockMvc.perform(get(GET_REQUEST_URI, COMPANY_NUMBER)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        assertTrue(capture.getOut().contains("request_id: %s".formatted(CONTEXT_ID)));
        assertFalse(capture.getOut().contains("request_id: %s".formatted(UNINITIALISED)));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldSuccessfullyInitialiseRequestIdWhenNoRequestIdProvided(CapturedOutput capture) throws Exception {
        // given
        when(companyPscService.retrievePscListSummaryFromDb(anyString(), anyInt(), anyBoolean(), anyInt()))
                .thenReturn(new PscList());

        // when
        mockMvc.perform(get(GET_REQUEST_URI, COMPANY_NUMBER)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        assertFalse(capture.getOut().contains("request_id: %s".formatted(UNINITIALISED)));
    }
}