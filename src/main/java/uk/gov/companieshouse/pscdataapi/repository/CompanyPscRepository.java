package uk.gov.companieshouse.pscdataapi.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

public interface CompanyPscRepository extends MongoRepository<PscDocument, String> {
    @Query("{'_id': ?0, 'delta.at':{$gte : { \"$date\" : \"?1\" } }}")
    List<PscDocument> findUpdatedPsc(String notificationId, String at);
}