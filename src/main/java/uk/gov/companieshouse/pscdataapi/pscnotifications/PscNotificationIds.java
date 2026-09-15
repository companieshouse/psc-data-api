package uk.gov.companieshouse.pscdataapi.pscnotifications;

import java.util.List;

public class PscNotificationIds {

    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public PscNotificationIds ids(List<String> ids) {
        this.ids = ids;
        return this;
    }
}