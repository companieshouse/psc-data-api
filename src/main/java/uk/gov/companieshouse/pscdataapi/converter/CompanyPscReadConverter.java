package uk.gov.companieshouse.pscdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.pscdataapi.models.PscData;

@ReadingConverter
public class CompanyPscReadConverter extends ReadConverter<PscData> {

    public CompanyPscReadConverter(ObjectMapper objectMapper, Class<PscData> objectClass) {
        super(objectMapper, objectClass);
    }
}