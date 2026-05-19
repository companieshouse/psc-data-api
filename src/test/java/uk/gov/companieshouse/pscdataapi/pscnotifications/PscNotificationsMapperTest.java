package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.companieshouse.api.psc_notifications.DateOfBirth;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.api.psc_notifications.NotificationListLinkTypes;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.pscdataapi.models.Links;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.pscnotifications.mappers.DateOfBirthMapper;
import uk.gov.companieshouse.pscdataapi.pscnotifications.mappers.ItemsMapper;
import uk.gov.companieshouse.pscdataapi.pscnotifications.mappers.LinksMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PscNotificationsMapperTest {

    private PscNotificationsMapper mapper;

    @Mock
    private ItemsMapper itemsMapper;
    @Mock
    private DateOfBirthMapper dobMapper;
    @Mock
    private LinksMapper linksMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new PscNotificationsMapper(itemsMapper, dobMapper, linksMapper);
    }

    @Test
    void testMapPscNotificationMapper_returnsMappedNotificationList_whenFirstNotificationAndDataPresent() {
        PscDocument firstNotification = new PscDocument();
        PscData pscData = new PscData();
        pscData.setKind("personal-notification");
        pscData.setName("Test Name");
        pscData.setLinks(new Links());

        PscSensitiveData sensitiveData = new PscSensitiveData();
        uk.gov.companieshouse.pscdataapi.models.DateOfBirth modelDob =
                new uk.gov.companieshouse.pscdataapi.models.DateOfBirth();
        modelDob.setMonth(1);
        modelDob.setYear(2000);
        sensitiveData.setDateOfBirth(modelDob);

        firstNotification.setData(pscData);
        firstNotification.setSensitiveData(sensitiveData);

        List<PscDocument> documents = List.of(firstNotification);

        DateOfBirth mappedDob = new DateOfBirth().month(1).year(2000);
        NotificationListLinkTypes mappedLinks = new NotificationListLinkTypes().self("self");
        List<PscNotificationSummary> mappedItems = List.of(new PscNotificationSummary().name("name"));

        when(dobMapper.map(modelDob)).thenReturn(mappedDob);
        when(linksMapper.map(pscData.getLinks())).thenReturn(mappedLinks);
        when(itemsMapper.map(documents)).thenReturn(mappedItems);

        PscNotificationsMapper.MapperRequest request = PscNotificationsMapper.MapperRequest.builder()
                .startIndex(0)
                .itemsPerPage(25)
                .firstNotification(firstNotification)
                .pscNotifications(documents)
                .totalResults(10)
                .activeCount(6)
                .inactiveCount(3)
                .ceasedCount(1)
                .build();

        Optional<NotificationList> result = mapper.mapPscNotifications(request);

        assertTrue(result.isPresent());
        NotificationList notificationList = result.get();
        assertEquals(6, notificationList.getActiveCount());
        assertEquals(1, notificationList.getCeasedCount());
        assertEquals(3, notificationList.getInactiveCount());
        assertEquals(0, notificationList.getStartIndex());
        assertEquals(25, notificationList.getItemsPerPage());
        assertEquals(10, notificationList.getTotalResults());
        assertEquals("Test Name", notificationList.getName());
        assertEquals(NotificationList.KindEnum.fromValue("personal-notification"), notificationList.getKind());
        assertEquals(mappedDob, notificationList.getDateOfBirth());
        assertEquals(mappedLinks, notificationList.getLinks());
        assertEquals(mappedItems, notificationList.getItems());

        verify(dobMapper).map(modelDob);
        verify(linksMapper).map(pscData.getLinks());
        verify(itemsMapper).map(documents);
    }

    @Test
    void testMapPscNotifications_returnsEmpty_whenFirstNotificationIsNull() {
        PscNotificationsMapper.MapperRequest request = PscNotificationsMapper.MapperRequest.builder()
                .startIndex(0)
                .itemsPerPage(25)
                .firstNotification(null)
                .pscNotifications(List.of())
                .totalResults(0)
                .activeCount(0)
                .inactiveCount(0)
                .ceasedCount(0)
                .build();

        Optional<NotificationList> result = mapper.mapPscNotifications(request);

        assertFalse(result.isPresent());
    }

    @Test
    void testMapPscNotifications_returnsEmpty_whenFirstNotificationDataIsNull() {
        PscDocument firstNotification = new PscDocument();
        firstNotification.setData(null);

        PscNotificationsMapper.MapperRequest request = PscNotificationsMapper.MapperRequest.builder()
                .startIndex(0)
                .itemsPerPage(25)
                .firstNotification(firstNotification)
                .pscNotifications(List.of(firstNotification))
                .totalResults(0)
                .activeCount(0)
                .inactiveCount(0)
                .ceasedCount(0)
                .build();

        Optional<NotificationList> result = mapper.mapPscNotifications(request);

        assertFalse(result.isPresent());
    }
}
