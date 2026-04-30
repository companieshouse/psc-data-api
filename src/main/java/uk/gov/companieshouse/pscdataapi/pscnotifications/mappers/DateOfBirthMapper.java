package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc.DateOfBirth;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Component
public class DateOfBirthMapper {

    Optional<DateOfBirth> map(Instant dateOfBirth){
        return ofNullable(dateOfBirth)
                .map(dob -> ZonedDateTime.ofInstant(dateOfBirth, ZoneOffset.UTC))
                .map(zdt -> new DateOfBirth()
                        .month(zdt.getMonthValue())
                        .year(zdt.getYear()));

    }
}
