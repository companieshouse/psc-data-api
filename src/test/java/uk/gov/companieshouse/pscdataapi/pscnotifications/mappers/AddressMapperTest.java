package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc_notifications.Address;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class AddressMapperTest {

    private final AddressMapper mapper = new AddressMapper();

    @Test
    void testAddressMapperMapsAddressWhenProvidedAddress() {
        uk.gov.companieshouse.pscdataapi.models.Address address =
                new uk.gov.companieshouse.pscdataapi.models.Address();
        address.setAddressLine1("123 Fire Street");
        address.setAddressLine2("Pontypandy");
        address.setCareOf("Glamorgan");
        address.setCountry("Wales");
        address.setLocality("South-West Wales");
        address.setPoBox("CF24 123");
        address.setPostalCode("CF12 122");
        address.setPremises("Fire House");
        address.setRegion("United Kingdom");

        Address address2 = mapper.map(address);

        assertSame("123 Fire Street", address2.getAddressLine1());
        assertSame("Pontypandy", address2.getAddressLine2());
        assertSame("Glamorgan", address2.getCareOf());
        assertSame("Wales", address2.getCountry());
        assertSame("South-West Wales", address2.getLocality());
        assertSame("CF24 123", address2.getPoBox());
        assertSame("CF12 122", address2.getPostalCode());
        assertSame("Fire House", address.getPremises());
        assertSame("United Kingdom", address.getRegion());
    }

    @Test
    void testAddressMapperReturnsNullWhenProvidedNullAddress() {
        assertNull(mapper.map(null));
    }
}
