package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.NameElements;

@Component
public class NameElementsMapper {

    NameElements map(uk.gov.companieshouse.pscdataapi.models.NameElements data) {
        return new NameElements()
                .forename(data.getForename())
                .middleName(data.getMiddleName())
                .surname(data.getSurname())
                .title(data.getTitle());
    }
}
