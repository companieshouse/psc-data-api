package uk.gov.companieshouse.pscdataapi.pscnotifications;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;


@Service
public class PscNotificationsService {

    private static final int DEFAULT_START_INDEX = 0;

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
        final int startIndex = getStartIndex(params.startIndex());
        final int itemsPerPage = itemsPerPageService.adjustItemsPerPage(
            params.itemsPerPage(), params.authPrivileges());
        final Filter filter = filterService.prepareFilter(params.filter(), pscId);
        final int totalResults = repository.countTotal(
            pscId, filter.filterEnabled(), filter.filterStatuses());
        final boolean sortByActiveThenCeased = sortingThresholdService.shouldSortByActiveThenResigned(
            totalResults, params.authPrivileges());

        List<PscDocument> documents;
        if (sortByActiveThenCeased) {
            PscNotificationIds notificationIds = repository.findPscNotificationsIds(
                    pscId, filter.filterEnabled(), filter.filterStatuses(), startIndex, itemsPerPage);
            documents = notificationIds.getIds().isEmpty()
                    ? List.of()
                    : repository.findFullPscNotifications(notificationIds.getIds());
        } else {
            documents = repository.findRecentPscNotifications(
                    pscId, filter.filterEnabled(), filter.filterStatuses(), startIndex, itemsPerPage);
        }
        PscDocument firstNotification = documents.isEmpty() ? null : documents.get(0);
        int ceasedCount = filter.filterEnabled() ? 0 : repository.countCeased(pscId);
        int inactiveCount = filter.filterEnabled() ? 0 : repository.countInactive(pscId);

        return mapper.mapPscNotifications(PscNotificationsMapper.MapperRequest.builder()
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .firstNotification(firstNotification)
                .pscNotifications(documents)
                .totalResults(totalResults)
                .activeCount(totalResults - ceasedCount)
                .ceasedCount(ceasedCount)
                .inactiveCount(inactiveCount)
                .build());
    }

    private static int getStartIndex(Integer requestStartIndex) {
        int startIndex;
        if (requestStartIndex == null) {
            startIndex = DEFAULT_START_INDEX;
        } else {
            startIndex = Math.abs(requestStartIndex);
        }

        return startIndex;
    }

}
