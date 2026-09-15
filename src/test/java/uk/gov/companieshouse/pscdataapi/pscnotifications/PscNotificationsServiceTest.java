package uk.gov.companieshouse.pscdataapi.pscnotifications;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

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
        service = new PscNotificationsService(repository, mapper, filterService, itemsPerPageService,
                sortingThresholdService);
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

        Filter filter = new Filter(false, Collections.emptyList());
        when(itemsPerPageService.adjustItemsPerPage(itemsPerPage, null)).thenReturn(itemsPerPage);
        when(filterService.prepareFilter(null, pscId)).thenReturn(filter);
        when(repository.countTotal(pscId, false, filter.filterStatuses())).thenReturn(2);
        when(sortingThresholdService.shouldSortByActiveThenResigned(2, null)).thenReturn(false);
        when(repository.findRecentPscNotifications(pscId, false, filter.filterStatuses(), startIndex, itemsPerPage))
                .thenReturn(documents);
        when(repository.countCeased(pscId)).thenReturn(0);
        when(repository.countInactive(pscId)).thenReturn(0);
        when(mapper.mapPscNotifications(any(PscNotificationsMapper.MapperRequest.class)))
                .thenReturn(Optional.of(mappedNotificationList));

        Optional<NotificationList> result = service.getPscNotifications(request);

        assertTrue(result.isPresent());
        assertSame(mappedNotificationList, result.get());

        verify(repository).countTotal(pscId, false, filter.filterStatuses());
        verify(repository).findRecentPscNotifications(pscId, false, filter.filterStatuses(), startIndex, itemsPerPage);

        ArgumentCaptor<PscNotificationsMapper.MapperRequest> mapperRequestCaptor =
                ArgumentCaptor.forClass(PscNotificationsMapper.MapperRequest.class);
        verify(mapper).mapPscNotifications(mapperRequestCaptor.capture());

        PscNotificationsMapper.MapperRequest captured = mapperRequestCaptor.getValue();
        assertEquals(startIndex, captured.startIndex());
        assertEquals(itemsPerPage, captured.itemsPerPage());
        assertEquals(documents, captured.pscNotifications());
        assertEquals(2, captured.totalResults());
        assertSame(documents.get(0), captured.firstNotification());
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

        Filter filter = new Filter(false, Collections.emptyList());
        when(itemsPerPageService.adjustItemsPerPage(itemsPerPage, null)).thenReturn(itemsPerPage);
        when(filterService.prepareFilter(null, pscId)).thenReturn(filter);
        when(repository.countTotal(pscId, false, filter.filterStatuses())).thenReturn(0);
        when(sortingThresholdService.shouldSortByActiveThenResigned(0, null)).thenReturn(false);
        when(repository.findRecentPscNotifications(pscId, false, filter.filterStatuses(), startIndex, itemsPerPage))
                .thenReturn(documents);
        when(repository.countCeased(pscId)).thenReturn(0);
        when(repository.countInactive(pscId)).thenReturn(0);
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

    @Test
    void testGetPscNotificationsUsesDefaultsWhenPaginationParamsProvidedAsNull() {
        PscNotificationsRequest request = new PscNotificationsRequest("11234", null, null, null, null);

        Filter filter = new Filter(false, Collections.emptyList());
        when(itemsPerPageService.adjustItemsPerPage(null, null)).thenReturn(35);
        when(filterService.prepareFilter(null, "11234")).thenReturn(filter);
        when(repository.countTotal("11234", false, filter.filterStatuses())).thenReturn(0);
        when(sortingThresholdService.shouldSortByActiveThenResigned(0, null)).thenReturn(false);
        when(repository.findRecentPscNotifications("11234", false, filter.filterStatuses(), 0, 35))
                .thenReturn(List.of());
        when(repository.countCeased("11234")).thenReturn(0);
        when(repository.countInactive("11234")).thenReturn(0);
        when(mapper.mapPscNotifications(any())).thenReturn(Optional.empty());

        service.getPscNotifications(request);

        ArgumentCaptor<PscNotificationsMapper.MapperRequest> captor =
                ArgumentCaptor.forClass(PscNotificationsMapper.MapperRequest.class);
        verify(mapper).mapPscNotifications(captor.capture());

        assertEquals(0, captor.getValue().startIndex());
        assertEquals(35, captor.getValue().itemsPerPage());
        assertTrue(captor.getValue().pscNotifications().isEmpty());
    }

    @Test
    void testGetPscNotificationsUsesSortedIdsWhenSortingThresholdApplies() {
        String pscId = "12345";
        int startIndex = 2;
        int itemsPerPage = 10;
        List<String> notificationIds = List.of("notification-1", "notification-2");
        List<PscDocument> documents = List.of(new PscDocument(), new PscDocument());
        Filter filter = new Filter(false, Collections.emptyList());

        PscNotificationsRequest request = PscNotificationsRequest.builder()
                .pscId(pscId)
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .build();

        when(itemsPerPageService.adjustItemsPerPage(itemsPerPage, null)).thenReturn(itemsPerPage);
        when(filterService.prepareFilter(null, pscId)).thenReturn(filter);
        when(repository.countTotal(pscId, false, filter.filterStatuses())).thenReturn(2);
        when(sortingThresholdService.shouldSortByActiveThenResigned(2, null)).thenReturn(true);
        when(repository.findPscNotificationsIds(pscId, false, filter.filterStatuses(), startIndex, itemsPerPage))
                .thenReturn(new PscNotificationIds().ids(notificationIds));
        when(repository.findFullPscNotifications(notificationIds)).thenReturn(documents);
        when(repository.countCeased(pscId)).thenReturn(1);
        when(repository.countInactive(pscId)).thenReturn(1);
        when(mapper.mapPscNotifications(any())).thenReturn(Optional.of(new NotificationList()));

        service.getPscNotifications(request);

        verify(repository).findFullPscNotifications(notificationIds);
        verify(repository, never()).findRecentPscNotifications(pscId, false, filter.filterStatuses(), startIndex,
                itemsPerPage);

        ArgumentCaptor<PscNotificationsMapper.MapperRequest> captor =
                ArgumentCaptor.forClass(PscNotificationsMapper.MapperRequest.class);
        verify(mapper).mapPscNotifications(captor.capture());
        assertEquals(documents, captor.getValue().pscNotifications());
        assertSame(documents.get(0), captor.getValue().firstNotification());
        assertEquals(1, captor.getValue().activeCount());
        assertEquals(1, captor.getValue().ceasedCount());
        assertEquals(1, captor.getValue().inactiveCount());
    }

    @Test
    void testGetPscNotificationsDoesNotLoadDocumentsWhenSortedIdsAreEmpty() {
        String pscId = "12345";
        int startIndex = 0;
        int itemsPerPage = 10;
        Filter filter = new Filter(false, Collections.emptyList());

        PscNotificationsRequest request = PscNotificationsRequest.builder()
                .pscId(pscId)
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .build();

        when(itemsPerPageService.adjustItemsPerPage(itemsPerPage, null)).thenReturn(itemsPerPage);
        when(filterService.prepareFilter(null, pscId)).thenReturn(filter);
        when(repository.countTotal(pscId, false, filter.filterStatuses())).thenReturn(0);
        when(sortingThresholdService.shouldSortByActiveThenResigned(0, null)).thenReturn(true);
        when(repository.findPscNotificationsIds(pscId, false, filter.filterStatuses(), startIndex, itemsPerPage))
                .thenReturn(new PscNotificationIds().ids(Collections.emptyList()));
        when(repository.countCeased(pscId)).thenReturn(0);
        when(repository.countInactive(pscId)).thenReturn(0);
        when(mapper.mapPscNotifications(any())).thenReturn(Optional.empty());

        Optional<NotificationList> result = service.getPscNotifications(request);

        assertTrue(result.isEmpty());
        verify(repository, never()).findFullPscNotifications(any());

        ArgumentCaptor<PscNotificationsMapper.MapperRequest> captor =
                ArgumentCaptor.forClass(PscNotificationsMapper.MapperRequest.class);
        verify(mapper).mapPscNotifications(captor.capture());
        assertTrue(captor.getValue().pscNotifications().isEmpty());
        assertNull(captor.getValue().firstNotification());
    }

    @Test
    void testGetPscNotificationsDoesNotCountCeasedOrInactiveForActiveFilter() {
        String pscId = "12345";
        int itemsPerPage = 20;
        List<PscDocument> documents = List.of(new PscDocument());
        List<String> inactiveStatuses = List.of("dissolved", "closed");
        Filter filter = new Filter(true, inactiveStatuses);

        PscNotificationsRequest request = PscNotificationsRequest.builder()
                .pscId(pscId)
                .itemsPerPage(itemsPerPage)
                .filter("active")
                .build();

        when(itemsPerPageService.adjustItemsPerPage(itemsPerPage, null)).thenReturn(itemsPerPage);
        when(filterService.prepareFilter("active", pscId)).thenReturn(filter);
        when(repository.countTotal(pscId, true, inactiveStatuses)).thenReturn(1);
        when(sortingThresholdService.shouldSortByActiveThenResigned(1, null)).thenReturn(false);
        when(repository.findRecentPscNotifications(pscId, true, inactiveStatuses, 0, itemsPerPage))
                .thenReturn(documents);
        when(mapper.mapPscNotifications(any())).thenReturn(Optional.of(new NotificationList()));

        service.getPscNotifications(request);

        verify(repository, never()).countCeased(pscId);
        verify(repository, never()).countInactive(pscId);

        ArgumentCaptor<PscNotificationsMapper.MapperRequest> captor =
                ArgumentCaptor.forClass(PscNotificationsMapper.MapperRequest.class);
        verify(mapper).mapPscNotifications(captor.capture());
        assertEquals(1, captor.getValue().activeCount());
        assertEquals(0, captor.getValue().ceasedCount());
        assertEquals(0, captor.getValue().inactiveCount());
    }

    @Test
    void testGetPscNotificationsConvertsNegativeStartIndexToPositiveValue() {
        String pscId = "12345";
        int itemsPerPage = 20;
        Filter filter = new Filter(false, Collections.emptyList());

        PscNotificationsRequest request = PscNotificationsRequest.builder()
                .pscId(pscId)
                .startIndex(-5)
                .itemsPerPage(itemsPerPage)
                .build();

        when(itemsPerPageService.adjustItemsPerPage(itemsPerPage, null)).thenReturn(itemsPerPage);
        when(filterService.prepareFilter(null, pscId)).thenReturn(filter);
        when(repository.countTotal(pscId, false, filter.filterStatuses())).thenReturn(0);
        when(sortingThresholdService.shouldSortByActiveThenResigned(0, null)).thenReturn(false);
        when(repository.findRecentPscNotifications(pscId, false, filter.filterStatuses(), 5, itemsPerPage))
                .thenReturn(Collections.emptyList());
        when(repository.countCeased(pscId)).thenReturn(0);
        when(repository.countInactive(pscId)).thenReturn(0);
        when(mapper.mapPscNotifications(any())).thenReturn(Optional.empty());

        service.getPscNotifications(request);

        ArgumentCaptor<PscNotificationsMapper.MapperRequest> captor =
                ArgumentCaptor.forClass(PscNotificationsMapper.MapperRequest.class);
        verify(mapper).mapPscNotifications(captor.capture());
        assertEquals(5, captor.getValue().startIndex());
    }
}
