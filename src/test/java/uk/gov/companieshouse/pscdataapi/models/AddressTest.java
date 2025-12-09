package uk.gov.companieshouse.pscdataapi.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AddressTest {

    @Test
    void shouldMapAllFieldsFromApiAddress() {
        uk.gov.companieshouse.api.psc.Address apiAddress =
            new uk.gov.companieshouse.api.psc.Address();
        apiAddress.setAddressLine1("123 Street");
        apiAddress.setAddressLine2("Apt 4B");
        apiAddress.setCountry("Country");
        apiAddress.setLocality("City");
        apiAddress.setPostalCode("PostalCode");
        apiAddress.setPremises("Premises");
        apiAddress.setRegion("Region");
        apiAddress.setCareOf("Care Of");
        apiAddress.setPoBox("PO Box");

        Address address = new Address(apiAddress);

        assertEquals("123 Street", address.getAddressLine1());
        assertEquals("Apt 4B", address.getAddressLine2());
        assertEquals("Country", address.getCountry());
        assertEquals("City", address.getLocality());
        assertEquals("PostalCode", address.getPostalCode());
        assertEquals("Premises", address.getPremises());
        assertEquals("Region", address.getRegion());
        assertEquals("Care Of", address.getCareOf());
        assertEquals("PO Box", address.getPoBox());
    }

    @Test
    void shouldHandleNullFieldsFromApiAddress() {
        uk.gov.companieshouse.api.psc.Address apiAddress =
            new uk.gov.companieshouse.api.psc.Address();

        Address address = new Address(apiAddress);

        assertNull(address.getAddressLine1());
        assertNull(address.getAddressLine2());
        assertNull(address.getCountry());
        assertNull(address.getLocality());
        assertNull(address.getPostalCode());
        assertNull(address.getPremises());
        assertNull(address.getRegion());
        assertNull(address.getCareOf());
        assertNull(address.getPoBox());
    }

    @Test
    void shouldMapAllFieldsFromUsualResidentialAddress() {
        uk.gov.companieshouse.api.psc.UsualResidentialAddress apiAddress =
            new uk.gov.companieshouse.api.psc.UsualResidentialAddress();
        apiAddress.setAddressLine1("123 Street");
        apiAddress.setAddressLine2("Apt 4B");
        apiAddress.setCountry("Country");
        apiAddress.setLocality("City");
        apiAddress.setPostalCode("PostalCode");
        apiAddress.setPremise("Premises");
        apiAddress.setRegion("Region");
        apiAddress.setCareOf("Care Of");
        apiAddress.setPoBox("PO Box");

        Address address = new Address(apiAddress);

        assertEquals("123 Street", address.getAddressLine1());
        assertEquals("Apt 4B", address.getAddressLine2());
        assertEquals("Country", address.getCountry());
        assertEquals("City", address.getLocality());
        assertEquals("PostalCode", address.getPostalCode());
        assertEquals("Premises", address.getPremises());
        assertEquals("Region", address.getRegion());
        assertEquals("Care Of", address.getCareOf());
        assertEquals("PO Box", address.getPoBox());
    }

    @Test
    void shouldHandleNullFieldsFromUsualResidentialAddress() {
        uk.gov.companieshouse.api.psc.UsualResidentialAddress apiAddress =
            new uk.gov.companieshouse.api.psc.UsualResidentialAddress();

        Address address = new Address(apiAddress);

        assertNull(address.getAddressLine1());
        assertNull(address.getAddressLine2());
        assertNull(address.getCountry());
        assertNull(address.getLocality());
        assertNull(address.getPostalCode());
        assertNull(address.getPremises());
        assertNull(address.getRegion());
        assertNull(address.getCareOf());
        assertNull(address.getPoBox());
    }

    @Test
    void shouldReturnFormattedStringWithAllFieldsPopulated() {
        Address address = new Address();
        address.setAddressLine1("123 Street");
        address.setAddressLine2("Apt 4B");
        address.setCountry("Country");
        address.setLocality("City");
        address.setPostalCode("PostalCode");
        address.setPremises("Premises");
        address.setRegion("Region");
        address.setCareOf("Care Of");
        address.setPoBox("PO Box");

        String expected = "Address{addressLine1='123 Street', addressLine2='Apt 4B', country='Country', locality='City', postalCode='PostalCode', premises='Premises', region='Region', careOf='Care Of', poBox='PO Box'}";
        assertEquals(expected, address.toString());
    }

    @Test
    void shouldReturnFormattedStringWithNullFields() {
        Address address = new Address();

        String expected = "Address{addressLine1='null', addressLine2='null', country='null', locality='null', postalCode='null', premises='null', region='null', careOf='null', poBox='null'}";
        assertEquals(expected, address.toString());
    }

    @Test
    void shouldBeEqualWhenAllFieldsAreIdentical() {
        Address address1 = new Address();
        address1.setAddressLine1("123 Street");
        address1.setAddressLine2("Apt 4B");
        address1.setCountry("Country");
        address1.setLocality("City");
        address1.setPostalCode("PostalCode");
        address1.setPremises("Premises");
        address1.setRegion("Region");
        address1.setCareOf("Care Of");
        address1.setPoBox("PO Box");

        Address address2 = new Address();
        address2.setAddressLine1("123 Street");
        address2.setAddressLine2("Apt 4B");
        address2.setCountry("Country");
        address2.setLocality("City");
        address2.setPostalCode("PostalCode");
        address2.setPremises("Premises");
        address2.setRegion("Region");
        address2.setCareOf("Care Of");
        address2.setPoBox("PO Box");

        assertEquals(address1, address2);
    }

    @Test
    void shouldNotBeEqualWhenAnyFieldDiffers() {
        Address address1 = new Address();
        address1.setAddressLine1("123 Street");
        address1.setAddressLine2("Apt 4B");
        address1.setCountry("Country");
        address1.setLocality("City");
        address1.setPostalCode("PostalCode");
        address1.setPremises("Premises");
        address1.setRegion("Region");
        address1.setCareOf("Care Of");
        address1.setPoBox("PO Box");

        Address address2 = new Address();
        address2.setAddressLine1("456 Avenue");
        address2.setAddressLine2("Apt 4B");
        address2.setCountry("Country");
        address2.setLocality("City");
        address2.setPostalCode("PostalCode");
        address2.setPremises("Premises");
        address2.setRegion("Region");
        address2.setCareOf("Care Of");
        address2.setPoBox("PO Box");

        assertNotEquals(address1, address2);
    }

    @Test
    void shouldGenerateSameHashCodeForIdenticalAddresses() {
        Address address1 = new Address();
        address1.setAddressLine1("123 Street");
        address1.setAddressLine2("Apt 4B");
        address1.setCountry("Country");
        address1.setLocality("City");
        address1.setPostalCode("PostalCode");
        address1.setPremises("Premises");
        address1.setRegion("Region");
        address1.setCareOf("Care Of");
        address1.setPoBox("PO Box");

        Address address2 = new Address();
        address2.setAddressLine1("123 Street");
        address2.setAddressLine2("Apt 4B");
        address2.setCountry("Country");
        address2.setLocality("City");
        address2.setPostalCode("PostalCode");
        address2.setPremises("Premises");
        address2.setRegion("Region");
        address2.setCareOf("Care Of");
        address2.setPoBox("PO Box");

        assertEquals(address1.hashCode(), address2.hashCode());
    }

    @Test
    void shouldGenerateDifferentHashCodeForDifferentAddresses() {
        Address address1 = new Address();
        address1.setAddressLine1("123 Street");
        address1.setAddressLine2("Apt 4B");
        address1.setCountry("Country");
        address1.setLocality("City");
        address1.setPostalCode("PostalCode");
        address1.setPremises("Premises");
        address1.setRegion("Region");
        address1.setCareOf("Care Of");
        address1.setPoBox("PO Box");

        Address address2 = new Address();
        address2.setAddressLine1("456 Avenue");
        address2.setAddressLine2("Apt 4B");
        address2.setCountry("Country");
        address2.setLocality("City");
        address2.setPostalCode("PostalCode");
        address2.setPremises("Premises");
        address2.setRegion("Region");
        address2.setCareOf("Care Of");
        address2.setPoBox("PO Box");

        assertNotEquals(address1.hashCode(), address2.hashCode());
    }
}