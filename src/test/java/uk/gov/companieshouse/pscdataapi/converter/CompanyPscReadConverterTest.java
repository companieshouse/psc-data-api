package uk.gov.companieshouse.pscdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.pscdataapi.exceptions.FailedToConvertException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

class CompanyPscReadConverterTest {

    private static final String NOTIFICATION_ID = "123456";

    private CompanyPscReadConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CompanyPscReadConverter(new ObjectMapper());
    }

    @Test
    void canConvertDocument() {
        Document notificationIdNode = new Document("notification_id", NOTIFICATION_ID);
        Document document = new Document("external_data", notificationIdNode);
        FullRecordCompanyPSCApi pscApi = converter.convert(document);

        assertEquals(NOTIFICATION_ID, pscApi.getExternalData().getNotificationId());
    }

    @Test
    void conversionFails() {
        Document brokenDocument = new Document("unknown_property", "value");
        assertThrows(FailedToConvertException.class, () -> converter.convert(brokenDocument));
    }
}
