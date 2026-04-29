package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.stereotype.Repository;

@Repository
public class PscNotificationsRepository extends MongoRepository<PscNotificationDocument, String> {
}
