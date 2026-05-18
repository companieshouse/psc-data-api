package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc.NameElements;
import uk.gov.companieshouse.pscdataapi.models.PscData;

@Component
public class NameMapper {

    NameElements mapNameElements(PscData data) {
        return new NameElements()
                .forename(data.getName());
    }
}
