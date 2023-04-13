package uk.gov.companieshouse.pscdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import uk.gov.companieshouse.pscdataapi.exceptions.FailedToConvertException;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

public class CompanyPscWriteConverter implements Converter<PscDocument, BasicDBObject> {

    private final ObjectMapper objectMapper;

    public CompanyPscWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return psc BSON object.
     */
    @Override
    public BasicDBObject convert(PscDocument source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (Exception ex) {
            throw new FailedToConvertException(ex.getMessage());
        }
    }
}