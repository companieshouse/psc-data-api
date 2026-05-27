package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.pscnotifications.mappers.DateOfBirthMapper;
import uk.gov.companieshouse.pscdataapi.pscnotifications.mappers.ItemsMapper;
import uk.gov.companieshouse.pscdataapi.pscnotifications.mappers.LinksMapper;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Component
class PscNotificationsMapper {

    private final ItemsMapper itemsMapper;
    private final DateOfBirthMapper dobMapper;
    private final LinksMapper linksMapper;

    PscNotificationsMapper(ItemsMapper itemsMapper, DateOfBirthMapper dobMapper, LinksMapper linksMapper) {
        this.itemsMapper = itemsMapper;
        this.dobMapper = dobMapper;
        this.linksMapper = linksMapper;
    }

    Optional<NotificationList> mapPscNotifications(MapperRequest mapperRequest) {

        return ofNullable(mapperRequest.firstNotification())
                .flatMap(firstNotification -> ofNullable(firstNotification.getData())
                        .map(data -> new NotificationList()
                                .activeCount(mapperRequest.activeCount())
                                .ceasedCount(mapperRequest.ceasedCount())
                                .dateOfBirth(dobMapper.map(firstNotification.getSensitiveData().getDateOfBirth()))
                                .inactiveCount(mapperRequest.inactiveCount())
                                .items(itemsMapper.map(mapperRequest.pscNotifications()))
                                .itemsPerPage(mapperRequest.itemsPerPage())
                                .kind(NotificationList.KindEnum.PERSONAL_NOTIFICATION)
                                .links(linksMapper.map(data.getLinks()))
                                .name(data.getName())
                                .startIndex(mapperRequest.startIndex())
                                .totalResults(mapperRequest.totalResults())
                        ));
    }

    record MapperRequest(Integer startIndex, Integer itemsPerPage, PscDocument firstNotification,
                         List<PscDocument> pscNotifications, Integer totalResults, Integer activeCount,
                         Integer inactiveCount, Integer ceasedCount) {

        private MapperRequest(Builder builder) {
            this(builder.startIndex, builder.itemsPerPage, builder.firstNotification, builder.pscNotifications,
                    builder.totalResults, builder.activeCount, builder.inactiveCount, builder.ceasedCount);
        }

        static Builder builder() {
            return new Builder();
        }

        static final class Builder {
            private Integer startIndex;
            private Integer itemsPerPage;
            private PscDocument firstNotification;
            private List<PscDocument> pscNotifications;
            private Integer totalResults;
            private Integer activeCount;
            private Integer inactiveCount;
            private Integer ceasedCount;

            private Builder(){}

            Builder startIndex(Integer startIndex) {
                this.startIndex = startIndex;
                return this;
            }

            Builder itemsPerPage(Integer itemsPerPage) {
                this.itemsPerPage = itemsPerPage;
                return this;
            }

            Builder firstNotification(PscDocument firstNotification) {
                this.firstNotification = firstNotification;
                return this;
            }

            Builder pscNotifications(List<PscDocument> pscNotifications) {
                this.pscNotifications = pscNotifications;
                return this;
            }

            Builder totalResults(Integer totalResults) {
                this.totalResults = totalResults;
                return this;
            }

            Builder activeCount(Integer activeCount) {
                this.activeCount = activeCount;
                return this;
            }

            Builder inactiveCount(Integer inactiveCount) {
                this.inactiveCount = inactiveCount;
                return this;
            }

            Builder ceasedCount(Integer ceasedCount) {
                this.ceasedCount = ceasedCount;
                return this;
            }

            MapperRequest build() {
                return new MapperRequest(this);
            }
        }
    }
}
