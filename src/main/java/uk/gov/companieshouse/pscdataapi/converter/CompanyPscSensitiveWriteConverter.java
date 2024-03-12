package uk.gov.companieshouse.pscdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.api.converter.WriteConverter;
import uk.gov.companieshouse.api.psc.SensitiveData;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;

@WritingConverter
public class CompanyPscSensitiveWriteConverter extends WriteConverter<PscSensitiveData> {
    public CompanyPscSensitiveWriteConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
