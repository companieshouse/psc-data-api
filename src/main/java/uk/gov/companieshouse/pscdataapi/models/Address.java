package uk.gov.companieshouse.pscdataapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Address {
    @JsonProperty("address_line_1")
    private String addressLine1;
    @JsonProperty("address_line_2")
    private String addressLine2;
    @JsonProperty("country")
    private String country;
    @JsonProperty("locality")
    private String locality;
    @JsonProperty("postal_code")
    private String postalCode;
    @JsonProperty("premises")
    private String premises;
    @JsonProperty("region")
    private String region;
    @JsonProperty("care_of")
    private String careOf;
    @JsonProperty("po_box")
    private String poBox;

    public Address() {}

    /**
     * Contructor using SDK Address.
     * @param address API Address object.
     */
    public Address(uk.gov.companieshouse.api.psc.Address address) {
        this.addressLine1 = address.getAddressLine1();
        this.addressLine2 = address.getAddressLine2();
        this.country = address.getCountry();
        this.locality = address.getLocality();
        this.postalCode = address.getPostalCode();
        this.premises = address.getPremises();
        this.region = address.getRegion();
        this.careOf = address.getCareOf();
        this.poBox = address.getPoBox();
    }

    /**
     * Contructor using SDK URA.
     * @param address API URA object.
     */
    public Address(uk.gov.companieshouse.api.psc.UsualResidentialAddress address) {
        this.addressLine1 = address.getAddressLine1();
        this.addressLine2 = address.getAddressLine2();
        this.country = address.getCountry();
        this.locality = address.getLocality();
        this.postalCode = address.getPostalCode();
        this.premises = address.getPremise();
        this.region = address.getRegion();
        this.careOf = address.getCareOf();
        this.poBox = address.getPoBox();
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPremises() {
        return premises;
    }

    public void setPremises(String premises) {
        this.premises = premises;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCareOf() {
        return careOf;
    }

    public void setCareOf(String careOf) {
        this.careOf = careOf;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    @Override
    public String toString() {
        return "Address{"
                + "addressLine1='"
                + addressLine1
                + '\''
                + ", addressLine2='"
                + addressLine2
                + '\''
                + ", country='"
                + country
                + '\''
                + ", locality='"
                + locality
                + '\''
                + ", postalCode='"
                + postalCode
                + '\''
                + ", premises='"
                + premises
                + '\''
                + ", region='"
                + region
                + '\''
                + ", careOf='"
                + careOf
                + '\''
                + ", poBox='"
                + poBox
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
        Address address = (Address) object;
        return Objects.equals(addressLine1, address.addressLine1)
                && Objects.equals(addressLine2, address.addressLine2)
                && Objects.equals(country, address.country)
                && Objects.equals(locality, address.locality)
                && Objects.equals(postalCode, address.postalCode)
                && Objects.equals(premises, address.premises)
                && Objects.equals(region, address.region)
                && Objects.equals(careOf, address.careOf)
                && Objects.equals(poBox, address.poBox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressLine1, addressLine2, country, locality, postalCode,
                premises, region, careOf, poBox);
    }
}
