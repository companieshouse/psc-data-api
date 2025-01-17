package uk.gov.companieshouse.pscdataapi.converter;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

@WritingConverter
public class WriteConverter<S> implements Converter<S, BasicDBObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final ObjectMapper objectMapper;

    public WriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BasicDBObject convert(S source) {
        try {
            return BasicDBObject.parse(this.objectMapper.writeValueAsString(source));
        } catch (JsonProcessingException ex) {
            final String msg = "Failed to convert Java object to MongoDB document";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new SerDesException(msg, ex);
        }
    }
}
