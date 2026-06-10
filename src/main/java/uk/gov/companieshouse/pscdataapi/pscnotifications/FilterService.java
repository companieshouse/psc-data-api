package uk.gov.companieshouse.pscdataapi.pscnotifications;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Component
public class FilterService {

    private static final String ACTIVE = "active";
    private final List<String> INACTIVE_STATUSES = List.of("dissolved", "converted-closed", "closed");

    Filter prepareFilter(String filter, String pscId) {
        if (StringUtils.isNotBlank(filter)) {
            if (ACTIVE.equals(filter)) {
                return new Filter(true, INACTIVE_STATUSES);
            } else {
                throw new BadRequestException(
                        "Invalid filter parameter supplied: %s, psc ID: %s".formatted(filter, pscId));
            }
        } else {
            return new Filter(false, emptyList());
        }
    }

    Optional<PscDocument> findFirstActiveNotification(List<PscDocument> documents) {
        return documents.stream()
                .filter(document -> !INACTIVE_STATUSES.contains(document.getCompanyStatus()))
                .filter(document -> document.getData().getCeasedOn() == null)
                .findFirst();
    }
}
