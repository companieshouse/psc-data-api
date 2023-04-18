package uk.gov.companieshouse.pscdataapi.models;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class PscSensitiveData {
    @Field("usual_residential_address")
    private Address usualResidentialAddress;
    @Field("date_of_birth")
    private DateOfBirth dateOfBirth;

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

    @Override
    public String toString() {
        return "PscSensitiveData{"
                + "usualResidentialAddress="
                + usualResidentialAddress
                + ", dateOfBirth="
                + dateOfBirth
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
                && Objects.equals(dateOfBirth, that.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usualResidentialAddress, dateOfBirth);
    }
}
