package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

import java.util.List;
import java.util.Optional;


@Service
public class PscNotificationsService {

    private final PscNotificationsRepository repository;
    private final PscNotificationsMapper mapper;
    private final FilterService filterService;
    private final ItemsPerPageService itemsPerPageService;
    private final SortingThresholdService sortingThresholdService;

    PscNotificationsService(PscNotificationsRepository repository,
                            PscNotificationsMapper mapper,
                            FilterService filterService,
                            ItemsPerPageService itemsPerPageService,
                            SortingThresholdService sortingThresholdService) {
        this.repository = repository;
        this.mapper = mapper;
        this.filterService = filterService;
        this.itemsPerPageService = itemsPerPageService;
        this.sortingThresholdService = sortingThresholdService;
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
