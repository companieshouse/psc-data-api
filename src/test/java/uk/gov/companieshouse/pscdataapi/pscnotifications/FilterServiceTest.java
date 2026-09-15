package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterServiceTest {

    private final FilterService service = new FilterService();

    @Test
    void prepareFilterReturnsDisabledFilterForBlankValue() {
        Filter filter = service.prepareFilter(" ", "psc-123");

        assertFalse(filter.filterEnabled());
        assertTrue(filter.filterStatuses().isEmpty());
    }

    @Test
    void prepareFilterReturnsActiveFilter() {
        Filter filter = service.prepareFilter("active", "psc-123");

        assertTrue(filter.filterEnabled());
        assertEquals(java.util.List.of("dissolved", "converted-closed", "closed"), filter.filterStatuses());
    }

    @Test
    void prepareFilterRejectsUnknownFilter() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.prepareFilter("invalid", "psc-123"));

        assertEquals("Invalid filter parameter supplied: invalid, PSC ID: psc-123", exception.getMessage());
    }
}
