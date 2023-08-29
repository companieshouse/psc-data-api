package uk.gov.companieshouse.pscdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

import uk.gov.companieshouse.pscdataapi.exceptions.FailedToConvertException;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

class CompanyPscWriteConverterTest {

    private static final String PSC_ID = "pscId";

    private CompanyPscWriteConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CompanyPscWriteConverter(new ObjectMapper());
    }

    @Test
    void canConvertDocument() {
        PscDocument document = new PscDocument();
        document.setNotificationId(PSC_ID);

        BasicDBObject object = converter.convert(document);

        String json = object.toJson();
        assertTrue(json.contains(PSC_ID));
    }

    @Test
    void conversionFails() {
        assertThrows(FailedToConvertException.class, () -> converter.convert(null));
    }
}
