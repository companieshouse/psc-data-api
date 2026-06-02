package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc_notifications.NameElements;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class NameElementsMapperTest {

    private final NameElementsMapper mapper = new NameElementsMapper();

    @Test
    void testNameElementsMapperReturnsNameElementsWhenGivenValidInput() {
        uk.gov.companieshouse.pscdataapi.models.NameElements elements =
                new uk.gov.companieshouse.pscdataapi.models.NameElements();

        elements.setForename("Steve");
        elements.setMiddleName("Bing");
        elements.setSurname("Madden");
        elements.setTitle("Mr");

        NameElements mappedNameElements = mapper.map(elements);

        assertSame("Steve", mappedNameElements.getForename());
        assertSame("Bing", mappedNameElements.getMiddleName());
        assertSame("Madden", mappedNameElements.getSurname());
        assertSame("Mr", mappedNameElements.getTitle());
    }

    @Test
    void testNameElementsMapperReturnsNullWhenGivenNullInput() {
        assertNull(mapper.map(null));
    }
}
