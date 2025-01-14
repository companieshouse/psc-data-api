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
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CompanyPscSensitiveWriteConverterTest {

    @Autowired
    private ObjectMapper objectMapper;

    private CompanyPscSensitiveWriteConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CompanyPscSensitiveWriteConverter(objectMapper);
    }

    @Test
    void shouldConvertPscSensitiveDataObjectToBasicDBObjectAndKeepSnakeCasing() throws Exception {
        // given
        final String json = IOUtils.resourceToString("/sensitive_data.json", StandardCharsets.UTF_8);
        PscSensitiveData pscSensitiveData = objectMapper.readValue(json, PscSensitiveData.class);

        final BasicDBObject expected = BasicDBObject.parse(json);

        // when
        final BasicDBObject actual = converter.convert(pscSensitiveData);

        // then
        assertEquals(expected, actual);
    }
}