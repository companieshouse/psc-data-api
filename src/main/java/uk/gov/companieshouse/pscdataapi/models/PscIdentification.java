package uk.gov.companieshouse.pscdataapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


public class PscIdentification {

    @JsonProperty("legal_form")
    private String legalForm;

    @JsonProperty("legal_authority")
    private String legalAuthority;

    @JsonProperty("country_registered")
    private String countryRegistered;

    @JsonProperty("place_registered")
    private String placeRegistered;

    @JsonProperty("registration_number")
    private String registrationNumber;

    public PscIdentification(){}

    /**
     * Contructor using SDK Identification.
     * @param identification API Address object.
     */
    public PscIdentification(uk.gov.companieshouse.api.psc.Identification identification) {
        this.legalForm = identification.getLegalForm();
        this.legalAuthority = identification.getLegalAuthority();
        this.countryRegistered = identification.getCountryRegistered();
        this.placeRegistered = identification.getPlaceRegistered();
        this.registrationNumber = identification.getRegistrationNumber();

    }

    public String getLegalForm() {
        return legalForm;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
    }

    public String getLegalAuthority() {
        return legalAuthority;
    }

    public void setLegalAuthority(String legalAuthority) {
        this.legalAuthority = legalAuthority;
    }

    public String getCountryRegistered() {
        return countryRegistered;
    }

    public void setCountryRegistered(String countryRegistered) {
        this.countryRegistered = countryRegistered;
    }

    public String getPlaceRegistered() {
        return placeRegistered;
    }

    public void setPlaceRegistered(String placeRegistered) {
        this.placeRegistered = placeRegistered;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    @Override
    public String toString() {
        return "PscIdentification{"
                + "legalForm='"
                + legalForm
                + '\''
                + ", legalAuthority='"
                + legalAuthority
                + '\''
                + ", countryRegistered='"
                + countryRegistered
                + '\''
                + ", placeRegistered='"
                + placeRegistered
                + '\''
                + ", registrationNumber='"
                + registrationNumber
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
        PscIdentification that = (PscIdentification) object;
        return Objects.equals(legalForm, that.legalForm)
                && Objects.equals(legalAuthority, that.legalAuthority)
                && Objects.equals(countryRegistered, that.countryRegistered)
                && Objects.equals(placeRegistered, that.placeRegistered)
                && Objects.equals(registrationNumber, that.registrationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                legalForm, legalAuthority, countryRegistered, placeRegistered, registrationNumber);
    }
}
