package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static uk.gov.companieshouse.pscdataapi.interceptor.AuthenticationHelperImpl.hasInternalAppPrivileges;

@Service
class SortingThresholdService {

    private final int internalSortingThreshold;
    private final int externalSortingThreshold;

    SortingThresholdService(@Value("${psc-notifications.sorting-threshold-internal}") final int internalSortingThreshold,
            @Value("${psc-notifications.sorting-threshold-external}") final int externalSortingThreshold) {
        this.internalSortingThreshold = internalSortingThreshold;
        this.externalSortingThreshold = externalSortingThreshold;
    }

    boolean shouldSortByActiveThenResigned(int totalResults, String authPrivileges) {
        int sortingThreshold = hasInternalAppPrivileges(authPrivileges) ? internalSortingThreshold : externalSortingThreshold;
        return sortingThreshold == -1 || totalResults <= sortingThreshold;
    }
}
