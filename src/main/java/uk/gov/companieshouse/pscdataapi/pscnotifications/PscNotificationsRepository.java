package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.stereotype.Repository;

@Repository
interface PscNotificationsRepository extends MongoRepository<PscNotificationDocument, String> {

    List<PscNotificationDocument> findAll();

    PscNotificationDocument findById(String pscId);
}
