package uk.gov.companieshouse.pscdataapi.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class PersonsWithSignificantControlTest {

    @Test
    void shouldSetAndGetNotifications() {
        PersonsWithSignificantControl model = new PersonsWithSignificantControl();

        model.setNotifications("notifications-link");

        assertEquals("notifications-link", model.getNotifications());
    }

    @Test
    void shouldHandleNullNotifications() {
        PersonsWithSignificantControl model = new PersonsWithSignificantControl();

        assertNull(model.getNotifications());
    }
}