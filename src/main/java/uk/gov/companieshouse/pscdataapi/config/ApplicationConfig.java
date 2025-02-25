package uk.gov.companieshouse.pscdataapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.pscdataapi.converter.CompanyPscReadConverter;
import uk.gov.companieshouse.pscdataapi.converter.CompanyPscSensitiveReadConverter;
import uk.gov.companieshouse.pscdataapi.converter.CompanyPscSensitiveWriteConverter;
import uk.gov.companieshouse.pscdataapi.converter.CompanyPscWriteConverter;
import uk.gov.companieshouse.pscdataapi.converter.EnumWriteConverter;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.models.PscData;
import uk.gov.companieshouse.pscdataapi.models.PscSensitiveData;
import uk.gov.companieshouse.pscdataapi.serialization.LocalDateDeSerializer;
import uk.gov.companieshouse.pscdataapi.serialization.LocalDateSerializer;

@Configuration
public class ApplicationConfig {

    private final String apiKey;
    private final String kafkaApiUrl;
    private final String metricsApiUrl;
    private final String exemptionsApiUrl;
    private final String verificationStateApiUrl;

    public ApplicationConfig(@Value("${api.key}") String apiKey,
                             @Value("${kafka.api.url}") String kafkaApiUrl,
                             @Value("${metrics.api.url}") String metricsApiUrl,
                             @Value("${exemptions.api.url}") String exemptionsApiUrl,
                             @Value("${verification-state.api.url}") final String verificationStateApiUrl) {
        this.apiKey = apiKey;
        this.kafkaApiUrl = kafkaApiUrl;
        this.metricsApiUrl = metricsApiUrl;
        this.exemptionsApiUrl = exemptionsApiUrl;
        this.verificationStateApiUrl = verificationStateApiUrl;
    }

    /*
    These custom converters allow us to only use @JsonProperty instead of needing the @Field annotation also on fields
    that require snake casing. Without these custom converters, and in the absence of the @Field annotation, we would
    get camel casing on fields with multiple words.
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
    public Supplier<InternalApiClient> kafkaApiClientSupplier() {
        return () -> buildClient(kafkaApiUrl);
    }

    @Bean
    public Supplier<InternalApiClient> metricsApiClientSupplier() {
        return () -> buildClient(metricsApiUrl);
    }

    @Bean
    public Supplier<InternalApiClient> exemptionsApiClientSupplier() {
        return () -> buildClient(exemptionsApiUrl);
    }

    @Bean
    public Supplier<InternalApiClient> verificationStateApiClientSupplier() {
        return () -> {
            final var internalApiClient = buildClient(verificationStateApiUrl);
            internalApiClient.setInternalBasePath(verificationStateApiUrl);
            return internalApiClient;
        };
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

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

    private InternalApiClient buildClient(final String url) {
        ApiKeyHttpClient apiKeyHttpClient = new ApiKeyHttpClient(apiKey);
        apiKeyHttpClient.setRequestId(DataMapHolder.getRequestId());

        InternalApiClient internalApiClient = new InternalApiClient(apiKeyHttpClient);
        internalApiClient.setBasePath(url);

        return internalApiClient;
    }
}