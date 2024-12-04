package uk.gov.companieshouse.pscdataapi.models;

public record PscDeleteRequest (String companyNumber, String notificationId, String contextId, String kind, String deltaAt) {

}
