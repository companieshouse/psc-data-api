package uk.gov.companieshouse.pscdataapi.models;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class Links {

    @Field("self")
    private String self;

    @Field("statements")
    private String statements;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getStatements() {
        return statements;
    }

    public void setStatements(String statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "Links{"
                + "self='"
                + self
                + '\''
                + ", statements='"
                + statements
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
                && Objects.equals(statements, links.statements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, statements);
    }
}
