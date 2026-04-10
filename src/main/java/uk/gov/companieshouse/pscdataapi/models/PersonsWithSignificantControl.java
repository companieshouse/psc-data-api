package uk.gov.companieshouse.pscdataapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonsWithSignificantControl {

    @JsonProperty("self")
    private String self;

    @JsonProperty("notifications")
    private String notifications;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }
}
