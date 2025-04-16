package uk.gov.companieshouse.pscdataapi.exceptions;

public class MethodNotAllowedException extends RuntimeException {

    public MethodNotAllowedException(String message) {
        super(message);
    }
}