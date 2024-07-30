package uk.gov.companieshouse.pscdataapi.models;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class Links {

    @Field("self")
    private String self;

    @Field("statement")
    private String statement;

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

    @Override
    public String toString() {
        return "Links{"
                + "self='"
                + self
                + '\''
                + ", statement='"
                + statement
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
        Links links = (Links) object;
        return Objects.equals(self, links.self)
                && Objects.equals(statement, links.statement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, statement);
    }
}
