package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemsPerPageServiceTest {

    private ItemsPerPageService service;

    @BeforeEach
    void setUp() {
        service = new ItemsPerPageService(500);
    }

    @Test
    void adjustItemsPerPageUsesDefaultWhenValueIsNull() {
        assertEquals(35, service.adjustItemsPerPage(null, null));
    }

    @Test
    void adjustItemsPerPageUsesDefaultWhenValueIsZero() {
        assertEquals(35, service.adjustItemsPerPage(0, null));
    }

    @Test
    void adjustItemsPerPageCapsExternalRequestsAtFifty() {
        assertEquals(50, service.adjustItemsPerPage(100, null));
    }

    @Test
    void adjustItemsPerPageUsesInternalMaximumForInternalRequests() {
        assertEquals(100, service.adjustItemsPerPage(100, "internal-app"));
        assertEquals(500, service.adjustItemsPerPage(600, "internal-app"));
    }

    @Test
    void adjustItemsPerPageConvertsNegativeValuesToPositive() {
        assertEquals(20, service.adjustItemsPerPage(-20, null));
    }
}
