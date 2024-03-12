package uk.gov.companieshouse.pscdataapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.api.CompanyMetricsApiService;
import uk.gov.companieshouse.api.converter.EnumWriteConverter;
import uk.gov.companieshouse.api.psc.SensitiveData;
import uk.gov.companieshouse.pscdataapi.converter.CompanyPscReadConverter;
import uk.gov.companieshouse.pscdataapi.converter.CompanyPscSensitiveReadConverter;
import uk.gov.companieshouse.pscdataapi.converter.CompanyPscSensitiveWriteConverter;
import uk.gov.companieshouse.pscdataapi.converter.CompanyPscWriteConverter;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.serialization.LocalDateDeSerializer;
import uk.gov.companieshouse.pscdataapi.serialization.LocalDateSerializer;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Configuration
public class ApplicationConfig {

    /**
     * mongoCustomConversions.
     *
     * @return MongoCustomConversions.
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        ObjectMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(List.of(new CompanyPscWriteConverter(objectMapper),
                new CompanyPscSensitiveWriteConverter(objectMapper),
                new CompanyPscReadConverter(objectMapper, PscData.class),
                new CompanyPscSensitiveReadConverter(objectMapper, PscSensitiveData.class),
                new EnumWriteConverter()));
    }

    @Bean
    public InternalApiClient internalApiClient() {
        return ApiSdkManager.getPrivateSDK();
    }

    @Bean
    public CompanyMetricsApiService companyMetricsApiService() {
        return new CompanyMetricsApiService();
    }



    /**
     * Mongo DB Object Mapper.
     *
     * @return ObjectMapper.
     */
    private ObjectMapper mongoDbObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
