package uk.gov.companieshouse.pscdataapi.pscnotifications.mappers;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.IdentityVerificationDetails;
import uk.gov.companieshouse.pscdataapi.models.PscIdentityVerificationDetails;

@Component
public class IdentityVerificationDetailsMapper {

    IdentityVerificationDetails map(PscIdentityVerificationDetails details) {
        if (details == null) return null;

        IdentityVerificationDetails ivd = new IdentityVerificationDetails();

        ivd.antiMoneyLaunderingSupervisoryBodies(details.getAntiMoneyLaunderingSupervisoryBodies());
        ivd.appointmentVerificationEndOn(details.getAppointmentVerificationEndOn());
        ivd.appointmentVerificationStartOn(details.getAppointmentVerificationStartOn());
        ivd.appointmentVerificationStatementDate(details.getAppointmentVerificationStatementDate());
        ivd.appointmentVerificationStatementDueOn(details.getAppointmentVerificationStatementDueOn());
        ivd.authorisedCorporateServiceProviderName(details.getAuthorisedCorporateServiceProviderName());
        ivd.identityVerifiedOn(details.getIdentityVerifiedOn());
        ivd.preferredName(details.getPreferredName());

        return ivd;
    }
}
