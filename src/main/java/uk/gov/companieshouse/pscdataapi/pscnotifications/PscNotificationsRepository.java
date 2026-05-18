package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

import java.util.List;
import java.util.Optional;

@Repository
interface PscNotificationsRepository extends MongoRepository<PscDocument, String> {

    List<PscDocument> findAll();

    Optional<PscDocument> findById(String pscId);

    @Query(value = "{ $and: [ "
            + "{'psc_id': ?0 },"
            + "{ 'company_status': { $nin: ?2 } }"
            + "] }", count = true)
    int countTotal(String pscId);
}
