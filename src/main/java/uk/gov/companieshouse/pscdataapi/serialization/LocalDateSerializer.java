package uk.gov.companieshouse.pscdataapi.serialization;

import tools.jackson.core.JsonGenerator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class LocalDateSerializer extends ValueSerializer<LocalDate> {

    @Override
    public void serialize(LocalDate localDate, JsonGenerator jsonGenerator,
            SerializationContext serializerProvider) {
        if (localDate == null) {
            jsonGenerator.writeNull();
        } else {
            DateTimeFormatter dateTimeFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String format = localDate.atStartOfDay().format(dateTimeFormatter);
            jsonGenerator.writeRawValue("ISODate(\"" + format + "\")");
        }
    }
}
