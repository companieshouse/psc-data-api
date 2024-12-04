package uk.gov.companieshouse.pscdataapi.util;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;

public final class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(UTC);

    private DateUtils() {
    }

    public static boolean isDeltaStale(final String requestDeltaAt, final String existingDeltaAt) {
        return StringUtils.isNotBlank(existingDeltaAt) && OffsetDateTime.parse(requestDeltaAt, FORMATTER)
                .isBefore(OffsetDateTime.parse(existingDeltaAt, FORMATTER));
    }
}