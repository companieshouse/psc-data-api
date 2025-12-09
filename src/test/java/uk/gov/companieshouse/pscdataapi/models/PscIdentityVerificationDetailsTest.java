package uk.gov.companieshouse.pscdataapi.models;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

class PscIdentityVerificationDetailsTest {

    @Test
    void shouldHandleNullIdentityVerificationDetails() {
        PscIdentityVerificationDetails details = new PscIdentityVerificationDetails(null);

        assertNull(details.getAntiMoneyLaunderingSupervisoryBodies());
        assertNull(details.getAppointmentVerificationEndOn());
        assertNull(details.getAppointmentVerificationStatementDate());
        assertNull(details.getAppointmentVerificationStatementDueOn());
        assertNull(details.getAppointmentVerificationStartOn());
        assertNull(details.getAuthorisedCorporateServiceProviderName());
        assertNull(details.getIdentityVerifiedOn());
        assertNull(details.getPreferredName());
    }

    @Test
    void shouldMapAllFieldsFromIdentityVerificationDetails() {
        uk.gov.companieshouse.api.psc.IdentityVerificationDetails apiDetails =
            new uk.gov.companieshouse.api.psc.IdentityVerificationDetails();
        apiDetails.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body1", "Body2"));
        apiDetails.setAppointmentVerificationEndOn(LocalDate.of(2023, 10, 1));
        apiDetails.setAppointmentVerificationStatementDate(LocalDate.of(2023, 9, 1));
        apiDetails.setAppointmentVerificationStatementDueOn(LocalDate.of(2023, 11, 1));
        apiDetails.setAppointmentVerificationStartOn(LocalDate.of(2023, 8, 1));
        apiDetails.setAuthorisedCorporateServiceProviderName("Provider Name");
        apiDetails.setIdentityVerifiedOn(LocalDate.of(2023, 7, 1));
        apiDetails.setPreferredName("Preferred Name");

        PscIdentityVerificationDetails details = new PscIdentityVerificationDetails(apiDetails);

        assertEquals(List.of("Body1", "Body2"), details.getAntiMoneyLaunderingSupervisoryBodies());
        assertEquals(LocalDate.of(2023, 10, 1), details.getAppointmentVerificationEndOn());
        assertEquals(LocalDate.of(2023, 9, 1), details.getAppointmentVerificationStatementDate());
        assertEquals(LocalDate.of(2023, 11, 1), details.getAppointmentVerificationStatementDueOn());
        assertEquals(LocalDate.of(2023, 8, 1), details.getAppointmentVerificationStartOn());
        assertEquals("Provider Name", details.getAuthorisedCorporateServiceProviderName());
        assertEquals(LocalDate.of(2023, 7, 1), details.getIdentityVerifiedOn());
        assertEquals("Preferred Name", details.getPreferredName());
    }

    @Test
    void shouldHandleNullFieldsFromIdentityVerificationDetails() {
        uk.gov.companieshouse.api.psc.IdentityVerificationDetails apiDetails =
            new uk.gov.companieshouse.api.psc.IdentityVerificationDetails();

        PscIdentityVerificationDetails details = new PscIdentityVerificationDetails(apiDetails);

        assertNull(details.getAntiMoneyLaunderingSupervisoryBodies());
        assertNull(details.getAppointmentVerificationEndOn());
        assertNull(details.getAppointmentVerificationStatementDate());
        assertNull(details.getAppointmentVerificationStatementDueOn());
        assertNull(details.getAppointmentVerificationStartOn());
        assertNull(details.getAuthorisedCorporateServiceProviderName());
        assertNull(details.getIdentityVerifiedOn());
        assertNull(details.getPreferredName());
    }

    @Test
    void shouldBeEqualWhenAllFieldsAreIdentical() {
        PscIdentityVerificationDetails details1 = new PscIdentityVerificationDetails();
        details1.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body1", "Body2"));
        details1.setAppointmentVerificationEndOn(LocalDate.of(2023, 10, 1));
        details1.setAppointmentVerificationStatementDate(LocalDate.of(2023, 9, 1));
        details1.setAppointmentVerificationStatementDueOn(LocalDate.of(2023, 11, 1));
        details1.setAppointmentVerificationStartOn(LocalDate.of(2023, 8, 1));
        details1.setAuthorisedCorporateServiceProviderName("Provider Name");
        details1.setIdentityVerifiedOn(LocalDate.of(2023, 7, 1));
        details1.setPreferredName("Preferred Name");

        PscIdentityVerificationDetails details2 = new PscIdentityVerificationDetails();
        details2.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body1", "Body2"));
        details2.setAppointmentVerificationEndOn(LocalDate.of(2023, 10, 1));
        details2.setAppointmentVerificationStatementDate(LocalDate.of(2023, 9, 1));
        details2.setAppointmentVerificationStatementDueOn(LocalDate.of(2023, 11, 1));
        details2.setAppointmentVerificationStartOn(LocalDate.of(2023, 8, 1));
        details2.setAuthorisedCorporateServiceProviderName("Provider Name");
        details2.setIdentityVerifiedOn(LocalDate.of(2023, 7, 1));
        details2.setPreferredName("Preferred Name");

        assertEquals(details1, details2);
    }

    @Test
    void shouldNotBeEqualWhenAnyFieldDiffers() {
        PscIdentityVerificationDetails details1 = new PscIdentityVerificationDetails();
        details1.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body1", "Body2"));
        details1.setAppointmentVerificationEndOn(LocalDate.of(2023, 10, 1));
        details1.setAppointmentVerificationStatementDate(LocalDate.of(2023, 9, 1));
        details1.setAppointmentVerificationStatementDueOn(LocalDate.of(2023, 11, 1));
        details1.setAppointmentVerificationStartOn(LocalDate.of(2023, 8, 1));
        details1.setAuthorisedCorporateServiceProviderName("Provider Name");
        details1.setIdentityVerifiedOn(LocalDate.of(2023, 7, 1));
        details1.setPreferredName("Preferred Name");

        PscIdentityVerificationDetails details2 = new PscIdentityVerificationDetails();
        details2.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body3"));
        details2.setAppointmentVerificationEndOn(LocalDate.of(2023, 10, 1));
        details2.setAppointmentVerificationStatementDate(LocalDate.of(2023, 9, 1));
        details2.setAppointmentVerificationStatementDueOn(LocalDate.of(2023, 11, 1));
        details2.setAppointmentVerificationStartOn(LocalDate.of(2023, 8, 1));
        details2.setAuthorisedCorporateServiceProviderName("Provider Name");
        details2.setIdentityVerifiedOn(LocalDate.of(2023, 7, 1));
        details2.setPreferredName("Preferred Name");

        assertNotEquals(details1, details2);
    }

    @Test
    void shouldReturnFormattedStringWithAllFieldsPopulated() {
        PscIdentityVerificationDetails details = new PscIdentityVerificationDetails();
        details.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body1", "Body2"));
        details.setAppointmentVerificationEndOn(LocalDate.of(2023, 10, 1));
        details.setAppointmentVerificationStatementDate(LocalDate.of(2023, 9, 1));
        details.setAppointmentVerificationStatementDueOn(LocalDate.of(2023, 11, 1));
        details.setAppointmentVerificationStartOn(LocalDate.of(2023, 8, 1));
        details.setAuthorisedCorporateServiceProviderName("Provider Name");
        details.setIdentityVerifiedOn(LocalDate.of(2023, 7, 1));
        details.setPreferredName("Preferred Name");

        String expected = "PscIdentityVerificationDetails{"
            + "antiMoneyLaunderingSupervisoryBodies=[Body1, Body2]'"
            + "appointmentVerificationEndOn=2023-10-01'"
            + "appointmentVerificationStatementDate=2023-09-01'"
            + "appointmentVerificationStatementDueOn=2023-11-01'"
            + "appointmentVerificationStartOn=2023-08-01'"
            + "authorisedCorporateServiceProviderName=Provider Name'"
            + "identityVerifiedOn=2023-07-01'"
            + "preferredName=Preferred Name'"
            + '}';

        assertEquals(expected, details.toString());
    }

    @Test
    void shouldReturnFormattedStringWithNullFields() {
        PscIdentityVerificationDetails details = new PscIdentityVerificationDetails();

        String expected = "PscIdentityVerificationDetails{"
            + "antiMoneyLaunderingSupervisoryBodies=null'"
            + "appointmentVerificationEndOn=null'"
            + "appointmentVerificationStatementDate=null'"
            + "appointmentVerificationStatementDueOn=null'"
            + "appointmentVerificationStartOn=null'"
            + "authorisedCorporateServiceProviderName=null'"
            + "identityVerifiedOn=null'"
            + "preferredName=null'"
            + '}';

        assertEquals(expected, details.toString());
    }

    @Test
    void shouldGenerateSameHashCodeForIdenticalFields() {
        PscIdentityVerificationDetails details1 = new PscIdentityVerificationDetails();
        details1.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body1", "Body2"));
        details1.setAppointmentVerificationEndOn(LocalDate.of(2023, 10, 1));
        details1.setAppointmentVerificationStatementDate(LocalDate.of(2023, 9, 1));
        details1.setAppointmentVerificationStatementDueOn(LocalDate.of(2023, 11, 1));
        details1.setAppointmentVerificationStartOn(LocalDate.of(2023, 8, 1));
        details1.setAuthorisedCorporateServiceProviderName("Provider Name");
        details1.setIdentityVerifiedOn(LocalDate.of(2023, 7, 1));
        details1.setPreferredName("Preferred Name");

        PscIdentityVerificationDetails details2 = new PscIdentityVerificationDetails();
        details2.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body1", "Body2"));
        details2.setAppointmentVerificationEndOn(LocalDate.of(2023, 10, 1));
        details2.setAppointmentVerificationStatementDate(LocalDate.of(2023, 9, 1));
        details2.setAppointmentVerificationStatementDueOn(LocalDate.of(2023, 11, 1));
        details2.setAppointmentVerificationStartOn(LocalDate.of(2023, 8, 1));
        details2.setAuthorisedCorporateServiceProviderName("Provider Name");
        details2.setIdentityVerifiedOn(LocalDate.of(2023, 7, 1));
        details2.setPreferredName("Preferred Name");

        assertEquals(details1.hashCode(), details2.hashCode());
    }

    @Test
    void shouldGenerateDifferentHashCodeForDifferentFields() {
        PscIdentityVerificationDetails details1 = new PscIdentityVerificationDetails();
        details1.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body1", "Body2"));
        details1.setAppointmentVerificationEndOn(LocalDate.of(2023, 10, 1));
        details1.setAppointmentVerificationStatementDate(LocalDate.of(2023, 9, 1));
        details1.setAppointmentVerificationStatementDueOn(LocalDate.of(2023, 11, 1));
        details1.setAppointmentVerificationStartOn(LocalDate.of(2023, 8, 1));
        details1.setAuthorisedCorporateServiceProviderName("Provider Name");
        details1.setIdentityVerifiedOn(LocalDate.of(2023, 7, 1));
        details1.setPreferredName("Preferred Name");

        PscIdentityVerificationDetails details2 = new PscIdentityVerificationDetails();
        details2.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body3"));
        details2.setAppointmentVerificationEndOn(LocalDate.of(2023, 10, 2));
        details2.setAppointmentVerificationStatementDate(LocalDate.of(2023, 9, 2));
        details2.setAppointmentVerificationStatementDueOn(LocalDate.of(2023, 11, 2));
        details2.setAppointmentVerificationStartOn(LocalDate.of(2023, 8, 2));
        details2.setAuthorisedCorporateServiceProviderName("Different Provider");
        details2.setIdentityVerifiedOn(LocalDate.of(2023, 7, 2));
        details2.setPreferredName("Different Name");

        assertNotEquals(details1.hashCode(), details2.hashCode());
    }

    @Test
    void shouldGenerateHashCodeWithNullFields() {
        PscIdentityVerificationDetails details = new PscIdentityVerificationDetails();

        int hashCode = details.hashCode();

        assertEquals(Objects.hash(null, null, null, null, null, null, null, null), hashCode);
    }

}