package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class PscNotifications {

    private List<String> ids;

    public List<String> getIds() { return ids; }

    public PscNotifications ids(List<String> ids) {
        this.ids = ids;
        return this;
    }
}
