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
        final String authPrivileges = params.authPrivileges();
        final int startIndex = params.startIndex();
        final int itemsPerPage = params.itemsPerPage();
        final int adjustedItemsPerPage = itemsPerPageService.adjustItemsPerPage(params.itemsPerPage(), authPrivileges);

        Filter filter = filterService.prepareFilter(params.filter(), params.pscId());
        boolean filterEnabled = filter.isFilterEnabled();
        List<String> filterStatuses = filter.filterStatuses();

        final int totalResults = repository.countTotal(pscId, filterEnabled, filterStatuses);

        List<PscDocument> documents;

        if (sortingThresholdService.shouldSortByActiveThenCeased(totalResults, authPrivileges)) {
            List<String> notificationIds = repository.findPscNotificationIds(pscId, filterEnabled, filterStatuses,
                    startIndex, adjustedItemsPerPage).getIds();

            if (!notificationIds.isEmpty()) {
                documents = repository.findFullPscNotifications(notificationIds);
            } else {
                documents = List.of();
            }
        } else {
            documents = repository.findRecentPscNotifications(pscId, filterEnabled, filterStatuses, startIndex,
                    adjustedItemsPerPage);
        }

        PscDocument firstNotification = filterService.findFirstActiveNotification(documents)
                .orElseGet(() -> repository.findLatestNotification(pscId));

        return mapper.mapPscNotifications(PscNotificationsMapper.MapperRequest.builder()
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .firstNotification(firstNotification)
                .pscNotifications(documents)
                .totalResults(totalResults)
                .build());
    }
}
