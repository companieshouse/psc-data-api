package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PscNotificationsServiceTest {

    @Mock
    private PscNotificationsRepository repository;

    @Mock
    private PscNotificationsMapper mapper;

    @Mock
    private FilterService filterService;

    @Mock
    private ItemsPerPageService itemsPerPageService;

    @Mock
    private SortingThresholdService sortingThresholdService;

    private PscNotificationsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PscNotificationsService(repository, mapper, filterService, itemsPerPageService, sortingThresholdService);
    }

    @Test
    void testGetPscNotificationsReturnsMappedResultWhenMapperReturnsValue() {
        String pscId = "12345";
        int startIndex = 5;
        int itemsPerPage = 20;

        PscNotificationsRequest request = PscNotificationsRequest.builder()
                .pscId(pscId)
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .build();

        List<PscDocument> documents = List.of(new PscDocument(), new PscDocument());
        NotificationList mappedNotificationList = new NotificationList().totalResults(2);

        when(repository.countByPscId(pscId)).thenReturn(2);
        when(repository.findAllByPscId(pscId)).thenReturn(documents);
        when(mapper.mapPscNotifications(any(PscNotificationsMapper.MapperRequest.class)))
                .thenReturn(Optional.of(mappedNotificationList));

        Optional<NotificationList> result = service.getPscNotifications(request);

        assertTrue(result.isPresent());
        assertSame(mappedNotificationList, result.get());

        verify(repository).countByPscId(pscId);
        verify(repository).findAllByPscId(pscId);

        ArgumentCaptor<PscNotificationsMapper.MapperRequest> mapperRequestCaptor =
                ArgumentCaptor.forClass(PscNotificationsMapper.MapperRequest.class);
        verify(mapper).mapPscNotifications(mapperRequestCaptor.capture());

        PscNotificationsMapper.MapperRequest captured = mapperRequestCaptor.getValue();
        assertEquals(startIndex, captured.startIndex());
        assertEquals(itemsPerPage, captured.itemsPerPage());
        assertEquals(documents, captured.pscNotifications());
        assertEquals(2, captured.totalResults());
    }

    @Test
    void testGetPscNotificationsReturnsEmptyListWhenFindAllByPscIdReturnsEmptyList() {
        String pscId = "12345";
        int startIndex = 5;
        int itemsPerPage = 20;

        PscNotificationsRequest request = PscNotificationsRequest.builder()
                .pscId(pscId)
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .build();

        List<PscDocument> documents = Collections.emptyList();
        NotificationList mappedNotificationList = new NotificationList().totalResults(0);

        when(repository.findAllByPscId(pscId)).thenReturn(documents);
        when(mapper.mapPscNotifications(any(PscNotificationsMapper.MapperRequest.class)))
                .thenReturn(Optional.of(mappedNotificationList));

        Optional<NotificationList> result = service.getPscNotifications(request);

        assertTrue(result.isPresent());
        assertSame(mappedNotificationList, result.get());

        ArgumentCaptor<PscNotificationsMapper.MapperRequest> captor =
                ArgumentCaptor.forClass(PscNotificationsMapper.MapperRequest.class);
        verify(mapper).mapPscNotifications(captor.capture());

        PscNotificationsMapper.MapperRequest captured = captor.getValue();
        assertEquals(documents, captured.pscNotifications());
        assertTrue(captured.pscNotifications().isEmpty());
        assertNull(captured.firstNotification());

    }
}
