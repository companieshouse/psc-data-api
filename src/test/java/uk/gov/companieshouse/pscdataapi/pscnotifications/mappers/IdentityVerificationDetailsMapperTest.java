package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc_notifications.IdentityVerificationDetails;
import uk.gov.companieshouse.pscdataapi.models.PscIdentityVerificationDetails;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdentityVerificationDetailsMapperTest {

    private final IdentityVerificationDetailsMapper mapper = new IdentityVerificationDetailsMapper();

    @Test
    void testIdentityVerificationDetailsMapper_mapsIdentityVerificationDetails_whenGivenValidInput() {
        PscIdentityVerificationDetails details = new PscIdentityVerificationDetails();
        details.setAntiMoneyLaunderingSupervisoryBodies(new ArrayList<>());
        details.setAppointmentVerificationEndOn(LocalDate.of(2024, 10, 10));
        details.setAppointmentVerificationStartOn(LocalDate.of(2023, 10, 10));
        details.setAppointmentVerificationStatementDate(LocalDate.of(2024, 9, 10));
        details.setAppointmentVerificationStatementDueOn(LocalDate.of(2024, 10, 10));
        details.setAuthorisedCorporateServiceProviderName("Provider");
        details.setIdentityVerifiedOn(LocalDate.of(2023, 10, 10));
        details.setPreferredName("Name");

        IdentityVerificationDetails mappedDetails = mapper.map(details);

        assertEquals(new ArrayList<>(), mappedDetails.getAntiMoneyLaunderingSupervisoryBodies());
        assertEquals(LocalDate.of(2024, 10, 10), mappedDetails.getAppointmentVerificationEndOn());
        assertEquals(LocalDate.of(2023, 10, 10), mappedDetails.getAppointmentVerificationStartOn());
        assertEquals(LocalDate.of(2024, 9, 10), mappedDetails.getAppointmentVerificationStatementDate());
        assertEquals(LocalDate.of(2024, 10, 10), mappedDetails.getAppointmentVerificationStatementDueOn());
        assertEquals("Provider", mappedDetails.getAuthorisedCorporateServiceProviderName());
        assertEquals(LocalDate.of(2023, 10, 10), mappedDetails.getIdentityVerifiedOn());
        assertEquals("Name", mappedDetails.getPreferredName());
    }

    @Test
    void testIdentityVerificationDetailsMapper_returnsNull_whenProvidedNullIdentityVerificationDetails() {
        assertNull(mapper.map(null));
    }
}
