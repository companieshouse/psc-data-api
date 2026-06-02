package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

import java.util.List;

@Repository
interface PscNotificationsRepository extends MongoRepository<PscDocument, String> {

    List<PscDocument> findAll();

   List <PscDocument> findAllByPscId(String pscId);

    int countByPscId(String pscId);
}
