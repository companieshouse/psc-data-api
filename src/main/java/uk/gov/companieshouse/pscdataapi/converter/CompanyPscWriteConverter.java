package uk.gov.companieshouse.pscdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.pscdataapi.models.PscData;

@WritingConverter
public class CompanyPscWriteConverter extends WriteConverter<PscData> {

    public CompanyPscWriteConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}