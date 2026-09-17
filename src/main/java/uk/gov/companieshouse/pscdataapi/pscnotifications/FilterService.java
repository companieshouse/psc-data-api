package uk.gov.companieshouse.pscdataapi.pscnotifications;

import static java.util.Collections.emptyList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;

@Service
class FilterService {

    private static final String ACTIVE = "active";
    private static final List<String> INACTIVE_STATUSES = List.of(
            "dissolved",
            "converted-closed",
            "closed");

    Filter prepareFilter(String filter, String pscId) {
        if (StringUtils.isBlank(filter)) {
            return new Filter(false, emptyList());
        }

        if (ACTIVE.equals(filter)) {
            return new Filter(true, INACTIVE_STATUSES);
        }

        throw new BadRequestException(
                "Invalid filter parameter supplied: %s, PSC ID: %s".formatted(filter, pscId));
    }
}

record Filter(boolean filterEnabled, List<String> filterStatuses) {
}