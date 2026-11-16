package uk.gov.companieshouse.pscdataapi.pscnotifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.pscnotifications.mappers.DateOfBirthMapper;
import uk.gov.companieshouse.pscdataapi.pscnotifications.mappers.ItemsMapper;

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
    void outOfRangePageReturnsOkThroughServiceAndMapper() {
        PscNotificationsRepository repository = mock(PscNotificationsRepository.class);
        FilterService filterService = mock(FilterService.class);
        ItemsPerPageService pageSizeService = mock(ItemsPerPageService.class);
        SortingThresholdService sortingService = mock(SortingThresholdService.class);
        ItemsMapper itemsMapper = mock(ItemsMapper.class);
        PscNotificationsMapper mapper = new PscNotificationsMapper(
                itemsMapper, mock(DateOfBirthMapper.class));
        PscNotificationsService realService = new PscNotificationsService(
                repository, mapper, filterService, pageSizeService, sortingService);
        PscNotificationsController realController = new PscNotificationsController(realService);
        PscData data = new PscData();
        data.setName("PAGINATION TEST");
        PscDocument metadata = new PscDocument();
        metadata.setData(data);

        when(filterService.prepareFilter(null, "psc-pagination"))
                .thenReturn(new Filter(false, List.of()));
        when(pageSizeService.adjustItemsPerPage(35, null)).thenReturn(35);
        when(repository.countTotal("psc-pagination", false, List.of())).thenReturn(75);
        when(repository.findRecentPscNotifications("psc-pagination", false, List.of(), 3465, 35))
                .thenReturn(List.of());
        when(repository.findRecentPscNotifications("psc-pagination", false, List.of(), 0, 1))
                .thenReturn(List.of(metadata));
        when(itemsMapper.map(List.of())).thenReturn(List.of());

        ResponseEntity<NotificationList> response = realController.getPscNotifications(
                "psc-pagination", null, 3465, 35, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("PAGINATION TEST");
        assertThat(response.getBody().getTotalResults()).isEqualTo(75);
        assertThat(response.getBody().getStartIndex()).isEqualTo(3465);
        assertThat(response.getBody().getItems()).isEmpty();
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