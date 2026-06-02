package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc_notifications.CorporateIdent;
import uk.gov.companieshouse.pscdataapi.models.PscIdentification;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IdentificationMapperTest {

    private final IdentificationMapper mapper = new IdentificationMapper();

    @Test
    void testIdentificationMapperMapsIdentityWhenGivenValidInput() {
        PscIdentification identity = new PscIdentification();
        identity.setCountryRegistered("Wales");
        identity.setLegalAuthority("United Kingdom");
        identity.setLegalForm("Legal form");
        identity.setPlaceRegistered("Registered place");
        identity.setRegistrationNumber("12345");

        CorporateIdent mappedIdent = mapper.map(identity);

        assertSame("Wales", mappedIdent.getCountryRegistered());
        assertSame("United Kingdom", mappedIdent.getLegalAuthority());
        assertSame("Legal form", mappedIdent.getLegalForm());
        assertSame("Registered place", mappedIdent.getPlaceRegistered());
        assertSame("12345", mappedIdent.getRegistrationNumber());
    }

    @Test
    void testIdentificationMapperReturnsNullWhenGivenNullInput() {
        assertNull(mapper.map(null));
    }
}
