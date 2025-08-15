package uk.gov.companieshouse.pscdataapi.config;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import java.time.format.DateTimeParseException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.pscdataapi.exceptions.InternalDataException;
import uk.gov.companieshouse.pscdataapi.exceptions.NotFoundException;
import uk.gov.companieshouse.pscdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

@ControllerAdvice
public class ExceptionHandlerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    @ExceptionHandler(value = {NotFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(Exception ex) {
        LOGGER.error("Not found exception", ex, DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
    }

    @ExceptionHandler(value = {ServiceUnavailableException.class})
    public ResponseEntity<Object> handleServiceUnavailableException(Exception ex) {
        LOGGER.error("Service unavailable", ex, DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .build();
    }

    @ExceptionHandler(value = {BadRequestException.class, DateTimeParseException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleBadRequestException(Exception ex) {
        LOGGER.error("Bad request", ex, DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }

    @ExceptionHandler(value = {ConflictException.class})
    public ResponseEntity<Object> handleConflictException(Exception ex) {
        LOGGER.error("Conflict", ex, DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .build();
    }

    @ExceptionHandler(value = {InternalDataException.class})
    public ResponseEntity<Object> handleInternalDataException(Exception ex) {
        LOGGER.error("Internal Data Exception", ex, DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }

    @ExceptionHandler(value = {BadGatewayException.class})
    public ResponseEntity<Object> handleBadGatewayException(Exception ex) {
        LOGGER.error("Bad gateway", ex, DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .build();
    }

    @ExceptionHandler(value = {TransientDataAccessException.class})
    public ResponseEntity<Object> handleTransientDataAccessException(Exception ex) {
        LOGGER.info("Recoverable MongoDB exception; Cause: [%s]".formatted(ex.getMessage()), DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .build();
    }

    @ExceptionHandler(value = {DataAccessException.class})
    public ResponseEntity<Object> handleDataAccessException(Exception ex) {
        LOGGER.error("Non-recoverable MongoDB exception", ex, DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .build();
    }

    @ExceptionHandler(value = {SerDesException.class})
    public ResponseEntity<Object> handleSerDesException(Exception ex) {
        LOGGER.error("Serialisation/deserialisation exception occurred", ex, DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleException(Exception ex) {
        LOGGER.error("Unexpected exception occurred", ex, DataMapHolder.getLogMap());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}
