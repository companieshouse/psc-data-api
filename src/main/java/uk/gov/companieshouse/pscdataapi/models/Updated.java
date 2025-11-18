package uk.gov.companieshouse.pscdataapi.models;

import java.time.LocalDateTime;

public class Updated {

    private LocalDateTime at;
    private String by;

    public LocalDateTime getAt() {
        return at;
    }

    public Updated at(LocalDateTime at) {
        this.at = at;
        return this;
    }

    public String getBy() {
        return by;
    }

    public Updated by(String by) {
        this.by = by;
        return this;
    }
}
