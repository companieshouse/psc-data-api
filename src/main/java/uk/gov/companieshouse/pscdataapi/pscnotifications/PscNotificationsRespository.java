package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.stereotype.Repository;

@Repository
public class PscNotificationsRespository extends MongoRepository<PscNotificationDocument, String> {
}
