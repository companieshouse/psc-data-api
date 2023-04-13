package uk.gov.companieshouse.pscdataapi.models;

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
}
