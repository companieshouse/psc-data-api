package uk.gov.companieshouse.pscdataapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class PscSensitiveData {
    @JsonProperty("usual_residential_address")
    private Address usualResidentialAddress;
    @JsonProperty("date_of_birth")
    private DateOfBirth dateOfBirth;
    @JsonProperty("residential_address_is_same_as_service_address")
    private Boolean residentialAddressIsSameAsServiceAddress;
    @JsonProperty("internal_id")
    private Long internalId;

    public Address getUsualResidentialAddress() {
        return usualResidentialAddress;
    }

    public void setUsualResidentialAddress(Address usualResidentialAddress) {
        this.usualResidentialAddress = usualResidentialAddress;
    }

    public DateOfBirth getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(DateOfBirth dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Boolean getResidentialAddressIsSameAsServiceAddress() {
        return residentialAddressIsSameAsServiceAddress;
    }

    public void setResidentialAddressIsSameAsServiceAddress(
            Boolean residentialAddressIsSameAsServiceAddress) {
        this.residentialAddressIsSameAsServiceAddress
                = residentialAddressIsSameAsServiceAddress;
    }

    public Long getInternalId() {
        return internalId;
    }

    public void setInternalId(final Long internalId) {
        this.internalId = internalId;
    }

    @Override
    public String toString() {
        return "PscSensitiveData{"
                + "usualResidentialAddress="
                + usualResidentialAddress
                + ", dateOfBirth="
                + dateOfBirth
                + ", residentialAddressIsSameAsServiceAddress="
                + residentialAddressIsSameAsServiceAddress
                + ", internalId="
                + internalId
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
        PscSensitiveData that = (PscSensitiveData) object;
        return Objects.equals(usualResidentialAddress, that.usualResidentialAddress)
                && Objects.equals(dateOfBirth, that.dateOfBirth)
                && Objects.equals(residentialAddressIsSameAsServiceAddress,
                that.residentialAddressIsSameAsServiceAddress)
                && Objects.equals(internalId, that.internalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usualResidentialAddress, dateOfBirth,
                residentialAddressIsSameAsServiceAddress, internalId);
    }
}
