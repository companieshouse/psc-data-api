package uk.gov.companieshouse.pscdataapi.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerConfigTest {

    private ExceptionHandlerConfig exceptionHandlerConfigConfig;

    @Mock
    Logger logger;
    @Mock
    WebRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandlerConfigConfig = new ExceptionHandlerConfig(logger);
    }

    @Test
    void handleException() {
        ResponseEntity response = exceptionHandlerConfigConfig.handleException(new Exception("exception"), request);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(responseBody.get("message"), is("Unable to process the request."));
    }

    @Test
    void handleNotFoundException() {
        ResponseEntity response = exceptionHandlerConfigConfig.handleNotFoundException(new Exception("exception"), request);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(responseBody.get("message"), is("Resource not found."));
    }

    @Test
    void handleMethodNotAllowedException() {
        ResponseEntity response = exceptionHandlerConfigConfig.handleMethodNotAllowedException(new Exception("exception"),
                request);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatusCode(), is(HttpStatus.METHOD_NOT_ALLOWED));
        assertThat(responseBody.get("message"), is("Unable to process the request."));
    }

    @Test
    void handleServiceUnavailableException() {
        ResponseEntity response = exceptionHandlerConfigConfig.handleServiceUnavailableException(new Exception("exception"),
                request);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatusCode(), is(HttpStatus.SERVICE_UNAVAILABLE));
        assertThat(responseBody.get("message"), is("Service unavailable."));
    }

    @Test
    void handleBadRequestException() {
        ResponseEntity response = exceptionHandlerConfigConfig.handleBadRequestException(new Exception("exception"), request);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseBody.get("message"), is("Bad request."));
    }

    @Test
    void handleConflictException() {
        ResponseEntity response = exceptionHandlerConfigConfig.handleConflictException(new Exception("exception"), request);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatusCode(), is(HttpStatus.CONFLICT));
        assertThat(responseBody.get("message"), is("Conflict."));
    }

    @Test
    void handleBadGatewayException() {
        ResponseEntity response = exceptionHandlerConfigConfig.handleBadGatewayException(new Exception("exception"), request);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_GATEWAY));
        assertThat(responseBody.get("message"), is("Bad Gateway."));
    }
}
