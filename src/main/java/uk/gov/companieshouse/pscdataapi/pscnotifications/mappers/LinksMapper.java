package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.NotificationListLinkTypes;
import uk.gov.companieshouse.pscdataapi.models.Links;

@Component
public class LinksMapper {

    public NotificationListLinkTypes map(Links links) {
        if (links == null) return null;

        NotificationListLinkTypes apiLinks = new NotificationListLinkTypes();
        apiLinks.setSelf(links.getSelf());

        return apiLinks;
    }
}
