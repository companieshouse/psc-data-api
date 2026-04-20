package uk.gov.companieshouse.pscdataapi.models;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    @Test
    void shouldBeEqualWhenNotificationsAreIdentical() {
        PersonsWithSignificantControl obj1 = new PersonsWithSignificantControl();
        obj1.setNotifications("same");

        PersonsWithSignificantControl obj2 = new PersonsWithSignificantControl();
        obj2.setNotifications("same");

        assertEquals(obj1, obj2);
    }

    @Test
    void shouldNotBeEqualWhenNotificationsDiffer() {
        PersonsWithSignificantControl obj1 = new PersonsWithSignificantControl();
        obj1.setNotifications("one");

        PersonsWithSignificantControl obj2 = new PersonsWithSignificantControl();
        obj2.setNotifications("two");

        assertNotEquals(obj1, obj2);
    }

    @Test
    void shouldBeEqualWhenBothNotificationsAreNull() {
        PersonsWithSignificantControl obj1 = new PersonsWithSignificantControl();
        PersonsWithSignificantControl obj2 = new PersonsWithSignificantControl();

        assertEquals(obj1, obj2);
    }

    @Test
    void shouldGenerateSameHashCodeForIdenticalObjects() {
        PersonsWithSignificantControl obj1 = new PersonsWithSignificantControl();
        obj1.setNotifications("same");

        PersonsWithSignificantControl obj2 = new PersonsWithSignificantControl();
        obj2.setNotifications("same");

        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void shouldGenerateDifferentHashCodeForDifferentObjects() {
        PersonsWithSignificantControl obj1 = new PersonsWithSignificantControl();
        obj1.setNotifications("one");

        PersonsWithSignificantControl obj2 = new PersonsWithSignificantControl();
        obj2.setNotifications("two");

        assertNotEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void shouldGenerateHashCodeWithNullFields() {
        PersonsWithSignificantControl obj = new PersonsWithSignificantControl();

        int hashCode = obj.hashCode();
        assertEquals(Objects.hash((Object) null), hashCode);
    }
}