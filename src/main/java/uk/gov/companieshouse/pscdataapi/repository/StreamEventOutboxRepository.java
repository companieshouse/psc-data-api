package uk.gov.companieshouse.pscdataapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.pscdataapi.models.StreamEventOutboxDocument;

public interface StreamEventOutboxRepository extends MongoRepository<StreamEventOutboxDocument, String> {

    List<StreamEventOutboxDocument> findByNextAttemptAtLessThanEqualOrderByCreatedAtAsc(Instant now, Pageable pageable);
}