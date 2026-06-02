package uk.gov.companieshouse.pscdataapi.pscnotifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;

@ExtendWith(MockitoExtension.class)
class PscNotificationsControllerTest {

    @Mock
    private PscNotificationsService service;

    @InjectMocks
    private PscNotificationsController controller;

    @Test
    void getPscNotificationsReturnsOkWhenNotificationsFound() {
        String pscId = "psc-123";
        String filter = "active";
        Integer startIndex = 0;
        Integer itemsPerPage = 25;
        String authPrivileges = "*";

        NotificationList notificationList = new NotificationList();
        notificationList.totalResults(1);
        notificationList.itemsPerPage(itemsPerPage);
        notificationList.startIndex(startIndex);

        when(service.getPscNotifications(any(PscNotificationsRequest.class)))
                .thenReturn(Optional.of(notificationList));

        ResponseEntity<NotificationList> response = controller.getPscNotifications(
                pscId, filter, startIndex, itemsPerPage, authPrivileges);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(notificationList);

        ArgumentCaptor<PscNotificationsRequest> requestCaptor =
                ArgumentCaptor.forClass(PscNotificationsRequest.class);
        verify(service).getPscNotifications(requestCaptor.capture());

        PscNotificationsRequest captured = requestCaptor.getValue();
        assertThat(captured.pscId()).isEqualTo(pscId);
        assertThat(captured.filter()).isEqualTo(filter);
        assertThat(captured.startIndex()).isEqualTo(startIndex);
        assertThat(captured.itemsPerPage()).isEqualTo(itemsPerPage);
        assertThat(captured.authPrivileges()).isEqualTo(authPrivileges);
    }

    @Test
    void getPscNotificationsReturnsNotFoundWhenNoNotificationsFound() {
        String pscId = "psc-404";

        when(service.getPscNotifications(any(PscNotificationsRequest.class)))
                .thenReturn(Optional.empty());

        ResponseEntity<NotificationList> response = controller.getPscNotifications(
                pscId, null, null, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNull();

        ArgumentCaptor<PscNotificationsRequest> requestCaptor =
                ArgumentCaptor.forClass(PscNotificationsRequest.class);
        verify(service).getPscNotifications(requestCaptor.capture());

        PscNotificationsRequest captured = requestCaptor.getValue();
        assertThat(captured.pscId()).isEqualTo(pscId);
        assertThat(captured.filter()).isNull();
        assertThat(captured.startIndex()).isNull();
        assertThat(captured.itemsPerPage()).isNull();
        assertThat(captured.authPrivileges()).isNull();
    }

    @Test
    void getPscNotificationsReturnsBadRequestWhenServiceThrowsBadRequestException() {
        when(service.getPscNotifications(any(PscNotificationsRequest.class)))
                .thenThrow(new BadRequestException("Invalid filter"));

        ResponseEntity<NotificationList> response = controller.getPscNotifications(
                "psc-123", "invalid-filter", 0, 25, "*");

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNull();

        verify(service).getPscNotifications(any(PscNotificationsRequest.class));
    }
}