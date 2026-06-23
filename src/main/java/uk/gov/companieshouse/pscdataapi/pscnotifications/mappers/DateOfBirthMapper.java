package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.DateOfBirth;

@Component
public class DateOfBirthMapper {
    public DateOfBirth map(uk.gov.companieshouse.pscdataapi.models.DateOfBirth dob) {
        if (dob == null || isEmpty(dob)) return null;

        return new DateOfBirth()
                .month(dob.getMonth())
                .year(dob.getYear());
    }

    private boolean isEmpty(uk.gov.companieshouse.pscdataapi.models.DateOfBirth dob) {
        return dob.getDay() == null && dob.getMonth() == null && dob.getYear() == null;
    }
}
