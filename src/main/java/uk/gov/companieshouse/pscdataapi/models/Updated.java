package uk.gov.companieshouse.pscdataapi.models;

import java.time.LocalDate;

public class Updated {

    private LocalDate at;

    public LocalDate getAt() {
        return at;
    }

    public Updated setAt(LocalDate at) {
        this.at = at;
        return this;
    }
}
