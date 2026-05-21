package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc_notifications.NotificationListLinkTypes;
import uk.gov.companieshouse.pscdataapi.models.Links;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class LinksMapperTest {

    private final LinksMapper mapper = new LinksMapper();

    @Test
    void testLinksMapper_returnsNotificationListLinkTypes_whenGivenValidInput() {
        Links links = new Links();
        links.setSelf("self-123");

        NotificationListLinkTypes mappedLinks = mapper.map(links);

        assertSame("self-123", mappedLinks.getSelf());
    }

    @Test
    void testLinksMapper_returnsNull_whenGivenNullInput() {
        assertNull(mapper.map(null));
    }
}
