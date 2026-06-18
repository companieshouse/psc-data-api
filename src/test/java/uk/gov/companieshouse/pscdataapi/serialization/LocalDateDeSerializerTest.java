package uk.gov.companieshouse.pscdataapi.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
class LocalDateDeSerializerTest {

    @Mock
    private DeserializationContext deserializationContext;
    @Mock
    private JsonParser jsonParser;
    @Mock
    private JsonNode jsonNode;
    @Mock
    private JsonNode dateNode;

    private LocalDateDeSerializer deserializer;

    @BeforeEach
    void setUp() {
        deserializer = new LocalDateDeSerializer();
        when(jsonParser.readValueAsTree()).thenReturn(jsonNode);
        when(jsonNode.get("$date")).thenReturn(dateNode);
    }

    @Test
    void validIsoDateStringShouldDeserializeToLocalDate() {
        when(dateNode.asString()).thenReturn("2023-01-09T00:00:00Z");

        LocalDate result = deserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(LocalDate.of(2023, 1, 9), result);
    }

    @Test
    void validEpochMillisShouldDeserializeToLocalDate() {
        JsonNode numberLongNode = org.mockito.Mockito.mock(JsonNode.class);
        when(dateNode.get("$numberLong")).thenReturn(numberLongNode);
        when(numberLongNode.asLong()).thenReturn(-1431388800000L);

        LocalDate result = deserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(LocalDate.of(1924, 8, 23), result);
    }
}
