package uk.gov.companieshouse.pscdataapi.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import uk.gov.companieshouse.pscdataapi.models.PscData;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CompanyPscReadConverterTest {

    @Autowired
    private ObjectMapper objectMapper;

    private CompanyPscReadConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CompanyPscReadConverter(objectMapper, PscData.class);
    }

    @Test
    void shouldConvertMongoDocToObjectTheSameWayObjectMapperBeanDoes() throws Exception {
        // given
        final String json = IOUtils.resourceToString("/data.json", StandardCharsets.UTF_8);
        Document document = Document.parse(json);

        final PscData expected = objectMapper.readValue(json, PscData.class);

        // when
        final PscData actual = converter.convert(document);

        // then
        assertNotNull(actual);
        assertEquals(expected, actual);
        assertNotNull(actual.getKind());
    }
}