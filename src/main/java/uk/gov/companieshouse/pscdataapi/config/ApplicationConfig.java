package uk.gov.companieshouse.pscdataapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    public ApplicationConfig(@Value("${api.key}") String apiKey,
            @Value("${kafka.api.url}") String kafkaApiUrl,
            @Value("${metrics.api.url}") String metricsApiUrl,
            @Value("${exemptions.api.url}") String exemptionsApiUrl) {
        this.apiKey = apiKey;
        this.kafkaApiUrl = kafkaApiUrl;
        this.metricsApiUrl = metricsApiUrl;
        this.exemptionsApiUrl = exemptionsApiUrl;
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

    @Bean(name = "toolsObjectMapper")
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL).withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }

    private ObjectMapper mongoDbObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());

        return JsonMapper.builder()
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL).withValueInclusion(JsonInclude.Include.NON_NULL))
            .addModule(module).build();
    }

    private InternalApiClient buildClient(final String url) {
        ApiKeyHttpClient apiKeyHttpClient = new ApiKeyHttpClient(apiKey);
        apiKeyHttpClient.setRequestId(DataMapHolder.getRequestId());

        InternalApiClient internalApiClient = new InternalApiClient(apiKeyHttpClient);
        internalApiClient.setBasePath(url);

        return internalApiClient;
    }
}