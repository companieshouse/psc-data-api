package uk.gov.companieshouse.pscdataapi.serialization;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

public class LocalDateDeSerializer extends JsonDeserializer<LocalDate> {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext
            deserializationContext) {
        try {
            LOGGER.info("Deserialising dates", DataMapHolder.getLogMap());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            JsonNode jsonNode = jsonParser.readValueAsTree();
            JsonNode dateNode = jsonNode.get("$date");

            /* If textValue() returns a value we received a string of
             * format yyyy-MM-dd'T'HH:mm:ss'Z
             * and use dateTimeFormatter to return LocalDate.
             *
             * Otherwise we received a long of milliseconds away
             * from 01/01/1970 and need to return
             * a LocalDate without dateTimeFormatter.
             */
            return dateNode.textValue() != null
                    ? LocalDate.parse(dateNode.textValue(), dateTimeFormatter)
                    : LocalDate.ofInstant(Instant.ofEpochMilli(dateNode.get("$numberLong")
                            .asLong()), ZoneOffset.UTC);
        } catch (Exception ex) {
            final String msg = "Deserialization failed";
            LOGGER.error(msg, ex, DataMapHolder.getLogMap());
            throw new BadRequestException(msg);
        }
    }
}
