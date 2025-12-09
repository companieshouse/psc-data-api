package uk.gov.companieshouse.pscdataapi.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Objects;

import org.junit.jupiter.api.Test;

class DateOfBirthTest {

    @Test
    void shouldMapAllFieldsFromApiDateOfBirth() {
        uk.gov.companieshouse.api.psc.DateOfBirth apiDob = new uk.gov.companieshouse.api.psc.DateOfBirth();
        apiDob.setDay(15);
        apiDob.setMonth(6);
        apiDob.setYear(1990);

        DateOfBirth dob = new DateOfBirth(apiDob);

        assertEquals(15, dob.getDay());
        assertEquals(6, dob.getMonth());
        assertEquals(1990, dob.getYear());
    }

    @Test
    void shouldHandleNullFieldsFromApiDateOfBirth() {
        uk.gov.companieshouse.api.psc.DateOfBirth apiDob = new uk.gov.companieshouse.api.psc.DateOfBirth();

        DateOfBirth dob = new DateOfBirth(apiDob);

        assertNull(dob.getDay());
        assertNull(dob.getMonth());
        assertNull(dob.getYear());
    }

    @Test
    void testEqualsSameObject() {
        DateOfBirth dob = new DateOfBirth();
        assertEquals(dob, dob);
    }

    @Test
    void testEqualsEqualObjects() {
        DateOfBirth dob1 = new DateOfBirth();
        dob1.setDay(1);
        dob1.setMonth(1);
        dob1.setYear(2000);

        DateOfBirth dob2 = new DateOfBirth();
        dob2.setDay(1);
        dob2.setMonth(1);
        dob2.setYear(2000);

        assertEquals(dob1, dob2);
    }

    @Test
    void testEqualsDifferentObjects() {
        DateOfBirth dob1 = new DateOfBirth();
        dob1.setDay(1);
        dob1.setMonth(1);
        dob1.setYear(2000);

        DateOfBirth dob2 = new DateOfBirth();
        dob2.setDay(2);
        dob2.setMonth(2);
        dob2.setYear(2001);

        assertNotEquals(dob1, dob2);
    }

    @Test
    void shouldGenerateSameHashCodeForIdenticalDateOfBirths() {
        DateOfBirth dob1 = new DateOfBirth();
        dob1.setDay(15);
        dob1.setMonth(6);
        dob1.setYear(1990);

        DateOfBirth dob2 = new DateOfBirth();
        dob2.setDay(15);
        dob2.setMonth(6);
        dob2.setYear(1990);

        assertEquals(dob1.hashCode(), dob2.hashCode());
    }

    @Test
    void shouldGenerateDifferentHashCodeForDifferentDateOfBirths() {
        DateOfBirth dob1 = new DateOfBirth();
        dob1.setDay(15);
        dob1.setMonth(6);
        dob1.setYear(1990);

        DateOfBirth dob2 = new DateOfBirth();
        dob2.setDay(16);
        dob2.setMonth(7);
        dob2.setYear(1991);

        assertNotEquals(dob1.hashCode(), dob2.hashCode());
    }

    @Test
    void shouldGenerateHashCodeWithNullFields() {
        DateOfBirth dob = new DateOfBirth();

        int hashCode = dob.hashCode();

        assertEquals(Objects.hash(null, null, null), hashCode);
    }

}