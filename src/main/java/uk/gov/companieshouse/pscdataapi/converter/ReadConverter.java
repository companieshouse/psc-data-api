package uk.gov.companieshouse.pscdataapi.converter;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

@ReadingConverter
public class ReadConverter<T> implements Converter<Document, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final ObjectMapper objectMapper;
    private final Class<T> objectClass;

    public ReadConverter(ObjectMapper objectMapper, Class<T> objectClass) {
        this.objectMapper = objectMapper;
        this.objectClass = objectClass;
    }

    public T convert(Document source) {
        try {
            return this.objectMapper.readValue(source.toJson(), this.objectClass);
        } catch (JsonProcessingException ex) {
            final String msg = "Failed to convert MongoDB document to Java object";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new SerDesException(msg, ex);
        }
    }
}
