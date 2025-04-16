package uk.gov.companieshouse.pscdataapi.exceptions;

public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }
}