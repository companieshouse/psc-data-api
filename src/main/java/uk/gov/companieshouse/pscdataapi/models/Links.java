package uk.gov.companieshouse.pscdataapi.models;

import java.util.Objects;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Links {

    @Field("self")
    private String self;

    @Field("statement")
    private String statement;

    @Field("exemptions")
    private String exemptions;

    @JsonProperty("persons-with-significant-control")
    private PersonsWithSignificantControl personsWithSignificantControl;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getExemptions() {
        return exemptions;
    }

    public void setExemptions(String exemptions) {
        this.exemptions = exemptions;
    }

    public PersonsWithSignificantControl getPersonsWithSignificantControl() {
        return personsWithSignificantControl;
    }

    public void setPersonsWithSignificantControl(PersonsWithSignificantControl personsWithSignificantControl) {
        this.personsWithSignificantControl = personsWithSignificantControl;
    }

    @Override
    public String toString() {
        return "Links{"
                + "self='"
                + self
                + '\''
                + ", statement='"
                + statement
                + '\''
                + ", exemptions='"
                + exemptions
                + '\''
                + ", personsWithSignificantControl="
                + personsWithSignificantControl
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
        Links links = (Links) object;
        return Objects.equals(self, links.self)
                && Objects.equals(statement, links.statement)
                && Objects.equals(exemptions, links.exemptions)
                && Objects.equals(personsWithSignificantControl, links.personsWithSignificantControl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, statement, exemptions, personsWithSignificantControl);
    }
}
