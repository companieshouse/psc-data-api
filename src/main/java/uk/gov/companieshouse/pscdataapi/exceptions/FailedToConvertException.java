package uk.gov.companieshouse.pscdataapi.exceptions;

public class FailedToConvertException extends RuntimeException {
    public FailedToConvertException(String message) {
        super(message);
    }
}
