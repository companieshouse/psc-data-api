package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.PscDataApiApplication;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

import java.util.List;
import java.util.Optional;


@Service
public class PscNotificationsService {

    private final PscNotificationsRepository repository;
    private final PscNotificationsMapper mapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(PscDataApiApplication.APPLICATION_NAME_SPACE);

    PscNotificationsService(PscNotificationsRepository repository,
                            PscNotificationsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    Optional<NotificationList> getPscNotifications(PscNotificationsRequest params) {
        final String pscId = params.pscId();
        final int startIndex = params.startIndex();
        final int itemsPerPage = params.itemsPerPage();

        final int totalResults = repository.countByPscId(pscId);
        List<PscDocument> documents = repository.findAllByPscId(pscId);
        PscDocument firstNotification = documents.isEmpty() ? null : documents.getFirst();

        return mapper.mapPscNotifications(PscNotificationsMapper.MapperRequest.builder()
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .firstNotification(firstNotification)
                .pscNotifications(documents)
                .totalResults(totalResults)
                .build());
    }
}
