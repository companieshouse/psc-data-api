package uk.gov.companieshouse.pscdataapi.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import uk.gov.companieshouse.pscdataapi.models.PscData;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CompanyPscWriteConverterTest {

    @Autowired
    private ObjectMapper objectMapper;

    private CompanyPscWriteConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CompanyPscWriteConverter(objectMapper);
    }

    @Test
    void shouldConvertPscDataObjectToBasicDBObjectAndKeepSnakeCasing() throws Exception {
        // given
        final String json = IOUtils.resourceToString("/data.json", StandardCharsets.UTF_8);
        PscData pscData = objectMapper.readValue(json, PscData.class);

        final BasicDBObject expected = BasicDBObject.parse(json);

        // when
        final BasicDBObject actual = converter.convert(pscData);

        // then
        assertEquals(expected, actual);
    }
}