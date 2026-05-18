package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.CorporateIdent;
import uk.gov.companieshouse.pscdataapi.models.PscIdentification;

import static java.util.Optional.ofNullable;

@Component
public class IdentificationMapper {

    CorporateIdent map(PscIdentification identificationData) {
        return ofNullable(identificationData)
                .map(identification -> new CorporateIdent()
                        .countryRegistered(identification.getCountryRegistered())
                        .legalAuthority(identification.getLegalAuthority())
                        .legalForm(identification.getLegalForm())
                        .placeRegistered(identification.getPlaceRegistered())
                        .registrationNumber(identification.getRegistrationNumber()))
                .orElse(null);
    }
}

