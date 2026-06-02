package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.DateOfBirth;

@Component
public class DateOfBirthMapper {
    public DateOfBirth map(uk.gov.companieshouse.pscdataapi.models.DateOfBirth dob) {
        if (dob == null) return null;
        // Map fields as appropriate
        return new DateOfBirth()
                .month(dob.getMonth())
                .year(dob.getYear());
    }
}
