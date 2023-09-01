package uk.gov.companieshouse.pscdataapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import uk.gov.companieshouse.api.model.PscStatementDocument;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

public interface CompanyPscRepository extends MongoRepository<PscDocument, String> {
    @Query("{'_id': ?0, 'delta_at':{$gte : \"?1\" }}")
    List<PscDocument> findUpdatedPsc(String notificationId, String at);

    @Query("{'company_number' : ?0, '_id' : ?1}")
    Optional<PscDocument> getPscByCompanyNumberAndId(String companyNumber, String notificationId);

}
