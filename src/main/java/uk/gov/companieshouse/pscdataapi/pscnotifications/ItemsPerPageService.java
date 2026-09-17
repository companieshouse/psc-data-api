package uk.gov.companieshouse.pscdataapi.pscnotifications;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static uk.gov.companieshouse.pscdataapi.interceptor.AuthenticationHelperImpl.hasInternalAppPrivileges;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;


@Service
public class ItemsPerPageService {

    private static final int DEFAULT_ITEMS_PER_PAGE = 35;
    private static final int MAX_ITEMS_PER_PAGE_EXTERNAL = 50;

    private final int maxItemsPerPageInternal;

    public ItemsPerPageService(@Value("${psc-notifications.items-per-page-max-internal}") final int maxItemsPerPageInternal) {
        this.maxItemsPerPageInternal = maxItemsPerPageInternal;
    }

    public int adjustItemsPerPage(Integer itemsPerPage, String authPrivileges) {
        if (itemsPerPage == null || itemsPerPage == 0) {
            itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
        } else {
            int maxItemsPerPage = hasInternalAppPrivileges(authPrivileges) ?
                    maxItemsPerPageInternal : MAX_ITEMS_PER_PAGE_EXTERNAL;
            itemsPerPage = Math.min(Math.abs(itemsPerPage), maxItemsPerPage);
        }
        DataMapHolder.get().itemsPerPage(String.valueOf(itemsPerPage));
        return itemsPerPage;
    }
}