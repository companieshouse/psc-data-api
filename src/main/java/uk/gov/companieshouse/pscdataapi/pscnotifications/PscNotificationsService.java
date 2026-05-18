package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PscNotificationsService {

    private final PscNotificationsRepository repository;
    private final PscNotificationsMapper mapper;

    PscNotificationsService(PscNotificationsRepository repository,
                            PscNotificationsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    Optional<NotificationList> getPscNotifications(PscNotificationsRequest params) {
        final String pscId = params.pscId();
        final int startIndex = params.startIndex();
        final int itemsPerPage = params.itemsPerPage();

        final int totalResults = repository.countTotal(pscId);
        List<PscDocument> documents = repository.findAllById(Collections.singleton(pscId));


        return mapper.mapPscNotifications(PscNotificationsMapper.MapperRequest.builder()
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .pscNotifications(documents)
                .totalResults(totalResults)
                .build());
    }
}
