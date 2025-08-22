package uk.gov.companieshouse.pscdataapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.companieshouse.api.psc.IdentityVerificationDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class PscIdentityVerificationDetails {

    @JsonProperty("anti_money_laundering_supervisory_bodies")
    private List<String> antiMoneyLaunderingSupervisoryBodies;

    @JsonProperty("appointment_verification_end_on")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate appointmentVerificationEndOn;

    @JsonProperty("appointment_verification_statement_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate appointmentVerificationStatementDate;

    @JsonProperty("appointment_verification_statement_due_on")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate appointmentVerificationStatementDueOn;

    @JsonProperty("appointment_verification_start_on")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate appointmentVerificationStartOn;

    @JsonProperty("authorised_corporate_service_provider_name")
    private String authorisedCorporateServiceProviderName;

    @JsonProperty("identity_verified_on")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate identityVerifiedOn;

    @JsonProperty("preferred_name")
    private String preferredName;

    public PscIdentityVerificationDetails(){}

    /**
     * Constructor using SDK IdentityVerificationDetails.
     *
     * @param identityVerificationDetails API Address object.
     */
    public PscIdentityVerificationDetails(IdentityVerificationDetails identityVerificationDetails){
        this.antiMoneyLaunderingSupervisoryBodies = identityVerificationDetails.getAntiMoneyLaunderingSupervisoryBodies();
        this.appointmentVerificationEndOn = identityVerificationDetails.getAppointmentVerificationEndOn();
        this.appointmentVerificationStatementDate = identityVerificationDetails.getAppointmentVerificationStatementDate();
        this.appointmentVerificationStatementDueOn = identityVerificationDetails.getAppointmentVerificationStatementDueOn();
        this.appointmentVerificationStartOn = identityVerificationDetails.getAppointmentVerificationStartOn();
        this.authorisedCorporateServiceProviderName = identityVerificationDetails.getAuthorisedCorporateServiceProviderName();
        this.identityVerifiedOn = identityVerificationDetails.getIdentityVerifiedOn();
        this.preferredName = identityVerificationDetails.getPreferredName();
    }

    public List<String> getAntiMoneyLaunderingSupervisoryBodies() {
        return antiMoneyLaunderingSupervisoryBodies;
    }

    public void setAntiMoneyLaunderingSupervisoryBodies(List<String> antiMoneyLaunderingSupervisoryBodies) {
        this.antiMoneyLaunderingSupervisoryBodies = antiMoneyLaunderingSupervisoryBodies;
    }

    public LocalDate getAppointmentVerificationEndOn() {
        return appointmentVerificationEndOn;
    }

    public void setAppointmentVerificationEndOn(LocalDate appointmentVerificationEndOn) {
        this.appointmentVerificationEndOn = appointmentVerificationEndOn;
    }

    public LocalDate getAppointmentVerificationStatementDate() {
        return appointmentVerificationStatementDate;
    }

    public void setAppointmentVerificationStatementDate(LocalDate appointmentVerificationStatementDate) {
        this.appointmentVerificationStatementDate = appointmentVerificationStatementDate;
    }

    public LocalDate getAppointmentVerificationStatementDueOn() {
        return appointmentVerificationStatementDueOn;
    }

    public void setAppointmentVerificationStatementDueOn(LocalDate appointmentVerificationStatementDueOn) {
        this.appointmentVerificationStatementDueOn = appointmentVerificationStatementDueOn;
    }

    public LocalDate getAppointmentVerificationStartOn() {
        return appointmentVerificationStartOn;
    }

    public void setAppointmentVerificationStartOn(LocalDate appointmentVerificationStartOn) {
        this.appointmentVerificationStartOn = appointmentVerificationStartOn;
    }

    public String getAuthorisedCorporateServiceProviderName() {
        return authorisedCorporateServiceProviderName;
    }

    public void setAuthorisedCorporateServiceProviderName(String authorisedCorporateServiceProviderName) {
        this.authorisedCorporateServiceProviderName = authorisedCorporateServiceProviderName;
    }

    public LocalDate getIdentityVerifiedOn() {
        return identityVerifiedOn;
    }

    public void setIdentityVerifiedOn(LocalDate identityVerifiedOn) {
        this.identityVerifiedOn = identityVerifiedOn;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    @Override
    public String toString() {
        return "PscIdentityVerificationDetails{"
                + "antiMoneyLaunderingSupervisoryBodies="
                + antiMoneyLaunderingSupervisoryBodies
                + '\''
                + "appointmentVerificationEndOn="
                + appointmentVerificationEndOn
                + '\''
                + "appointmentVerificationStatementDate="
                + appointmentVerificationStatementDate
                + '\''
                + "appointmentVerificationStatementDueOn="
                + appointmentVerificationStatementDueOn
                + '\''
                + "appointmentVerificationStartOn="
                + appointmentVerificationStartOn
                + '\''
                + "authorisedCorporateServiceProviderName="
                + authorisedCorporateServiceProviderName
                + '\''
                + "identityVerifiedOn="
                + identityVerifiedOn
                + '\''
                + "preferredName="
                + preferredName
                + '\''
                + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PscIdentityVerificationDetails that = (PscIdentityVerificationDetails) object;
        return Objects.equals(antiMoneyLaunderingSupervisoryBodies, that.antiMoneyLaunderingSupervisoryBodies)
                && Objects.equals(appointmentVerificationEndOn, that.appointmentVerificationEndOn)
                && Objects.equals(appointmentVerificationStatementDate, that.appointmentVerificationStatementDate)
                && Objects.equals(appointmentVerificationStatementDueOn, that.appointmentVerificationStatementDueOn)
                && Objects.equals(appointmentVerificationStartOn, that.appointmentVerificationStartOn)
                && Objects.equals(authorisedCorporateServiceProviderName, that.authorisedCorporateServiceProviderName)
                && Objects.equals(identityVerifiedOn, that.identityVerifiedOn)
                && Objects.equals(preferredName, that.preferredName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                antiMoneyLaunderingSupervisoryBodies,
                appointmentVerificationEndOn,
                appointmentVerificationStatementDate,
                appointmentVerificationStatementDueOn,
                appointmentVerificationStartOn,
                authorisedCorporateServiceProviderName,
                identityVerifiedOn,
                preferredName);
    }
}
