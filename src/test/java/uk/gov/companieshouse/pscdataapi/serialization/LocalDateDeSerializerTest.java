package uk.gov.companieshouse.pscdataapi.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;

class LocalDateDeSerializerTest {

    private LocalDateDeSerializer deserializer;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        deserializer = new LocalDateDeSerializer();

        mapper = new ObjectMapper();
    }

    @Test
    void dateShouldDeserialize() throws Exception {

        String jsonTestString = "{\"date\":{\"$date\": \"2023-01-09T00:00:00Z\"}}";

        LocalDate returnedDate = deserialize(jsonTestString);
        assertEquals(LocalDate.of(2023, 1, 9), returnedDate);

    }

    @Test
    void longStringReturnsLong() throws Exception {

        String jsonTestString = "{\"date\":{\"$date\": {\"$numberLong\":\"-1431388800000\"}}}";

        LocalDate returnedDate = deserialize(jsonTestString);
        assertEquals(LocalDate.of(1924, 8, 23), returnedDate);

    }

    @Test
    void nullStringReturnsError() {

        String jsonTestString = null;

        assertThrows(NullPointerException.class, () -> {
            deserialize(jsonTestString);
        });
    }

    @Test
    void invalidStringReturnsError() {

        String jsonTestString = "{\"date\":{\"$date\": \"NotADate\"}}}";

        assertThrows(BadRequestException.class, () -> {
            deserialize(jsonTestString);
        });
    }

    private LocalDate deserialize(String jsonString) throws Exception {
        JsonParser parser = mapper.getFactory().createParser(jsonString);
        DeserializationContext deserializationContext = mapper.getDeserializationContext();

        parser.nextToken();
        parser.nextToken();
        parser.nextToken();

        return deserializer.deserialize(parser, deserializationContext);
    }

}
