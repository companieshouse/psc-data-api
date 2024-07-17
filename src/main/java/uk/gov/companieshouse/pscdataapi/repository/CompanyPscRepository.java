package uk.gov.companieshouse.pscdataapi.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

public interface CompanyPscRepository extends MongoRepository<PscDocument, String> {
    @Query("{'_id': ?0, 'delta_at':{$gte : \"?1\" }}")
    List<PscDocument> findUpdatedPsc(String notificationId, String at);

    @Query("{'_id' : ?1, 'company_number' : ?0}")
    Optional<PscDocument> getPscByCompanyNumberAndId(String companyNumber, String notificationId);

    @Query("{'company_number' : ?0}")
    Optional<PscDocument> findPscByCompanyNumber(String companyNumber);


    @Aggregation(pipeline = {
            "{'$match': { 'company_number': ?0} } }",
            "{'$sort': {'data.notified_on': -1, 'data.ceased_on': -1 } }",
            "{'$skip': ?1}",
            "{'$limit': ?2}",
            })
    Optional<List<PscDocument>> getPscDocumentList(String companyNumber,
                                                   Integer startIndex, Integer itemsPerPage);


    @Aggregation(pipeline = {
            "{'$match': { 'company_number' : ?0, "
                    + "$or:[ { '" + "data.ceased_on': { $gte : { \"$date\" : \"?2\" }} },"
                    + "{ 'data.ceased_on': {$exists: false }} ]} }",
            "{'$sort': {'data.notified_on': -1, 'data.ceased_on': -1 } }",
            "{'$skip': ?1}",
            "{'$limit': ?3}",
            })
    Optional<List<PscDocument>> getListSummaryRegisterView(
            String companyNumber, Integer startIndex, OffsetDateTime movedOn, Integer itemsPerPage);
}
