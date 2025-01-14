package uk.gov.companieshouse.pscdataapi.converter;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import org.springframework.core.convert.converter.Converter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

public class EnumWriteConverter implements Converter<Enum<?>, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    public String convert(Enum<?> source) {
        try {
            return source.toString();
        } catch (Exception ex) {
            final String msg = "Failed to convert Enum to String";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new SerDesException(msg, ex);
        }
    }
}
