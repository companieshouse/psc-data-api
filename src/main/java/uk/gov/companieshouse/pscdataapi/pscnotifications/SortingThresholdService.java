package uk.gov.companieshouse.pscdataapi.pscnotifications;

import static uk.gov.companieshouse.pscdataapi.interceptor.AuthenticationHelperImpl.hasInternalAppPrivileges;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SortingThresholdService {

    private final int internalSortingThreshold;
    private final int externalSortingThreshold;

    SortingThresholdService(@Value("500") final int internalSortingThreshold,
                            @Value("500") final int externalSortingThreshold) {
        this.internalSortingThreshold = internalSortingThreshold;
        this.externalSortingThreshold = externalSortingThreshold;
    }

    boolean shouldSortByActiveThenCeased(int totalResults, String authPrivileges) {
        int sortingThreshold = hasInternalAppPrivileges(authPrivileges) ? internalSortingThreshold : externalSortingThreshold;
        return sortingThreshold == -1 || totalResults <= sortingThreshold;
    }
}
