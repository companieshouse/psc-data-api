package uk.gov.companieshouse.pscdataapi.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonsWithSignificantControl {

    @JsonProperty("self")
    private String self;

    @JsonProperty("notifications")
    private String notifications;

    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) { 
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PersonsWithSignificantControl personsWithSignificantControl = (PersonsWithSignificantControl) object;
        return 
               Objects.equals(notifications, personsWithSignificantControl.notifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, notifications);
    }
}
