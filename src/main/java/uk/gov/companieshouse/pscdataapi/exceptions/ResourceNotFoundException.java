package uk.gov.companieshouse.pscdataapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResourceNotFoundException extends ResponseStatusException {

    public ResourceNotFoundException(HttpStatus status, String message) {
        super(status, message);
    }
}
