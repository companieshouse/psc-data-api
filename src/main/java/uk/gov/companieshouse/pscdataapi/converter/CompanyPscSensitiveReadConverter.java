package uk.gov.companieshouse.pscdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.converter.ReadConverter;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;

@ReadingConverter
public class CompanyPscSensitiveReadConverter extends ReadConverter<PscSensitiveData> {
    public CompanyPscSensitiveReadConverter(ObjectMapper objectMapper,
                                            Class<PscSensitiveData> objectClass) {
        super(objectMapper, objectClass);
    }
}
