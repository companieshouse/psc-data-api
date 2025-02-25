package uk.gov.companieshouse.pscdataapi.transform;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.api.model.psc.PscVerificationStateApi;
import uk.gov.companieshouse.api.model.psc.VerificationState;

@Mapper(componentModel = "spring")
public interface VerificationStateMapper {

    VerificationState mapToVerificationState(PscVerificationStateApi pscVerificationStateApi);

}
