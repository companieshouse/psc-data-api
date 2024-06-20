package uk.gov.companieshouse.pscdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.api.converter.WriteConverter;
import uk.gov.companieshouse.pscdataapi.exceptions.FailedToConvertException;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;

@WritingConverter
public class CompanyPscWriteConverter extends WriteConverter<PscData> {
    public CompanyPscWriteConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}