package uk.gov.companieshouse.pscdataapi.transform;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.api.model.psc.IdentityVerificationDetailsApi;
import uk.gov.companieshouse.api.model.psc.IdentityVerificationDetails;

@Mapper(componentModel = "spring")
public interface IdentityVerificationDetailsMapper {

    IdentityVerificationDetails mapToIdentityVerificationDetails(IdentityVerificationDetailsApi identityVerificationDetailsApi);

}
