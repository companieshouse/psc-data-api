package uk.gov.companieshouse.pscdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.pscdataapi.exceptions.FailedToConvertException;

public class CompanyPscReadConverter implements Converter<Document, FullRecordCompanyPSCApi> {

    private final ObjectMapper objectMapper;

    public CompanyPscReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Read convertor.
     * @param source source Document.
     * @return psc API object.
     */
    @Override
    public FullRecordCompanyPSCApi convert(Document source) {
        try {
            return objectMapper.readValue(source.toJson(), FullRecordCompanyPSCApi.class);
        } catch (Exception ex) {
            throw new FailedToConvertException(ex.getMessage());
        }
    }
}
