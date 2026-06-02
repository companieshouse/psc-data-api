package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.Address;

@Component
public class AddressMapper {
    public Address map(uk.gov.companieshouse.pscdataapi.models.Address address) {
        if (address == null) return null;
        return new Address()
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .careOf(address.getCareOf())
                .country(address.getCountry())
                .locality(address.getLocality())
                .poBox(address.getPoBox())
                .postalCode(address.getPostalCode())
                .premises(address.getPremises())
                .region(address.getRegion());
    }
}
