package uk.gov.companieshouse.pscdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.company.Data;
import uk.gov.companieshouse.api.converter.ReadConverter;
import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.pscdataapi.exceptions.FailedToConvertException;
import uk.gov.companieshouse.pscdataapi.models.PscData;

@ReadingConverter
public class CompanyPscReadConverter extends ReadConverter<PscData> {
    public CompanyPscReadConverter(ObjectMapper objectMapper, Class<PscData> objectClass) {
        super(objectMapper, objectClass);
    }
}