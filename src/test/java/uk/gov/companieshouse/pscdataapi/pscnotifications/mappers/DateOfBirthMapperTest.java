package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc_notifications.DateOfBirth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DateOfBirthMapperTest {

    private final DateOfBirthMapper mapper = new DateOfBirthMapper();

    @Test
    void testDateOfBirthMapperMapsDateOfBirthWhenGivenValidInput() {
        uk.gov.companieshouse.pscdataapi.models.DateOfBirth dob =
                new uk.gov.companieshouse.pscdataapi.models.DateOfBirth();

        dob.setMonth(10);
        dob.setYear(2023);

        DateOfBirth mappedDob = mapper.map(dob);

        assertEquals(10, mappedDob.getMonth());
        assertEquals(2023, mappedDob.getYear());
    }

    @Test
    void testDateOfBirthMapperReturnsNullWhenProvidedNullDateOfBirth() {
        assertNull(mapper.map(null));
    }
 }
