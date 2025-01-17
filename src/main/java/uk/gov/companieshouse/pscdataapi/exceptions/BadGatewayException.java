package uk.gov.companieshouse.pscdataapi.exceptions;

public class BadGatewayException extends RuntimeException {

    public BadGatewayException(String message, Throwable ex) {
        super(message, ex);
    }

}
