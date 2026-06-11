package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

import java.util.List;
import java.util.Optional;


@Service
public class PscNotificationsService {

    private static final int DEFAULT_START_INDEX = 0;
    private static final int DEFAULT_ITEMS_PER_PAGE = 35;

    private final PscNotificationsRepository repository;
    private final PscNotificationsMapper mapper;

    PscNotificationsService(PscNotificationsRepository repository,
                            PscNotificationsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    Optional<NotificationList> getPscNotifications(PscNotificationsRequest params) {
        final String pscId = params.pscId();
        final int startIndex = getStartIndex(params.startIndex());
        final int itemsPerPage = getItemsPerPage(params.itemsPerPage());

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

    private static int getStartIndex(Integer requestStartIndex) {
        int startIndex;
        if (requestStartIndex == null) {
            startIndex = DEFAULT_START_INDEX;
        } else {
            startIndex = Math.abs(requestStartIndex);
        }
        //DataMapHolder.get().startIndex(String.valueOf(startIndex));
        return startIndex;
    }

    private static int getItemsPerPage(Integer requestItemsPerPage) {
        int itemsPerPage;
        if (requestItemsPerPage == null) {
            itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
        } else {
            itemsPerPage = Math.abs(requestItemsPerPage);
        }
        return itemsPerPage;
    }
}
