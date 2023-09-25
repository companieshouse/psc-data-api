package uk.gov.companieshouse.pscdataapi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Identification {
    @Field("place_registered")
    private String placeRegistered;
    @Field("legal_authority")
    private String legalAuthority;
    @Field("registration_number")
    private String registrationNumber;
    @Field("country_registered")
    private String countryRegistered;
    @Field("legal_form")
    private String legalForm;

    public Identification() {}

    /**
     * Contructor using SDK Identification.
     * @param identification API Identification object.
     */

    public Identification(uk.gov.companieshouse.api.psc.Identification identification) {
        this.placeRegistered = identification.getPlaceRegistered();
        this.legalAuthority = identification.getLegalAuthority();
        this.registrationNumber = identification.getRegistrationNumber();
        this.countryRegistered = identification.getCountryRegistered();
        this.legalForm = identification.getLegalForm();
    }

    public String getPlaceRegistered() { return placeRegistered; }

    public void setPlaceRegistered(String placeRegistered) { this.placeRegistered = placeRegistered; }

    public String getLegalAuthority() { return legalAuthority; }

    public void setLegalAuthority(String legalAuthority) { this.legalAuthority = legalAuthority; }

    public String getRegistrationNumber() { return registrationNumber; }

    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getCountryRegistered() { return countryRegistered; }

    public void setCountryRegistered(String countryRegistered) { this.countryRegistered = countryRegistered; }

    public String getLegalForm() { return legalForm; }
    public void setLegalForm(String legalForm) { this.legalForm = legalForm; }

    @Override
    public String toString() {
        return "Identification{"
                +
                "placeRegistered='"
                + placeRegistered
                + '\''
                + ", legalAuthority='"
                + legalAuthority
                + '\''
                + ", registrationNumber='"
                + registrationNumber
                + '\''
                + ", countryRegistered='"
                + countryRegistered
                + '\''
                + ", legalForm='"
                + legalForm
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
        Identification identification = (Identification) object;
        return Objects.equals(placeRegistered, identification.placeRegistered)
                && Objects.equals(legalAuthority, identification.legalAuthority)
                && Objects.equals(registrationNumber, identification.registrationNumber)
                && Objects.equals(countryRegistered, identification.countryRegistered)
                && Objects.equals(legalForm, identification.legalForm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeRegistered, legalAuthority, registrationNumber, countryRegistered, legalForm);
    }
}
