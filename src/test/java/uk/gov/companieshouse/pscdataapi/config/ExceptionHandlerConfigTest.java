package uk.gov.companieshouse.pscdataapi.config;


import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.NoHandlerFoundException;
import uk.gov.companieshouse.pscdataapi.exceptions.*;

class ExceptionHandlerConfigTest {

    @Test
    void shouldReturnNotFoundStatusForNotFoundException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleNotFoundException(new NotFoundException("Resource not found"));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void shouldReturnNotFoundStatusForNoHandlerFoundException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleNotFoundException(new NoHandlerFoundException("GET", "/path", null));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void shouldReturnServiceUnavailableStatusForServiceUnavailableException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleServiceUnavailableException(new ServiceUnavailableException("Service unavailable"));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void shouldReturnBadRequestStatusForBadRequestException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleBadRequestException(new BadRequestException("Bad request"));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void shouldReturnBadRequestStatusForDateTimeParseException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleBadRequestException(new DateTimeParseException("Invalid date", "2023-01-01", 0));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void shouldReturnConflictStatusForConflictException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleConflictException(new ConflictException("Conflict occurred"));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void shouldReturnInternalServerErrorStatusForInternalDataException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleInternalDataException(new InternalDataException("Internal data error"));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void shouldReturnBadGatewayStatusForBadGatewayException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleBadGatewayException(new BadGatewayException("Bad gateway", null));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void shouldReturnBadGatewayStatusForTransientDataAccessException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleTransientDataAccessException(new TransientDataAccessException("Transient data access error") {});

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void shouldReturnBadGatewayStatusForDataAccessException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleDataAccessException(new DataAccessException("Data access error") {});

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void shouldReturnInternalServerErrorStatusForSerDesException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleSerDesException(new SerDesException("Serialization error", null));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }


    @Test
    void shouldReturnInternalServerErrorStatusForGenericException() {
        ExceptionHandlerConfig exceptionHandlerConfig = new ExceptionHandlerConfig();

        ResponseEntity<Object> response = exceptionHandlerConfig.handleException(new Exception("Generic error"));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }


}