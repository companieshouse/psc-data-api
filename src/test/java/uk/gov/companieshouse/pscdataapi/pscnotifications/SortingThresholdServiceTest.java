package uk.gov.companieshouse.pscdataapi.pscnotifications;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class SortingThresholdServiceTest {

    @Test
    void shouldSortWhenExternalResultsAreAtOrBelowThreshold() {
        SortingThresholdService service = new SortingThresholdService(100, 10);

        assertTrue(service.shouldSortByActiveThenResigned(10, null));
        assertFalse(service.shouldSortByActiveThenResigned(11, null));
    }

    @Test
    void shouldSortWhenInternalResultsAreAtOrBelowThreshold() {
        SortingThresholdService service = new SortingThresholdService(100, 10);

        assertTrue(service.shouldSortByActiveThenResigned(100, "internal-app"));
        assertFalse(service.shouldSortByActiveThenResigned(101, "internal-app"));
    }

    @Test
    void shouldAlwaysSortWhenExternalThresholdIsMinusOne() {
        SortingThresholdService service = new SortingThresholdService(100, -1);

        assertTrue(service.shouldSortByActiveThenResigned(1000, null));
    }

    @Test
    void shouldAlwaysSortWhenInternalThresholdIsMinusOne() {
        SortingThresholdService service = new SortingThresholdService(-1, 10);

        assertTrue(service.shouldSortByActiveThenResigned(1000, "internal-app"));
    }
}
