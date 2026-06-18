package uk.gov.companieshouse.pscdataapi.steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.service.CompanyExemptionsApiService;
import uk.gov.companieshouse.pscdataapi.service.CompanyMetricsApiService;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = CucumberSpringConfig.Initializer.class)
@AutoConfigureTestRestTemplate
public class CucumberSpringConfig {

    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:8.0.26"));

    static {
        mongoDBContainer.start();
    }

    @MockitoBean
    private ChsKafkaApiService chsKafkaApiService;

    @MockitoBean
    private CompanyMetricsApiService companyMetricsApiService;

    @MockitoBean
    private CompanyExemptionsApiService companyExemptionsApiService;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(final ConfigurableApplicationContext applicationContext) {
            // Short timeouts so "database is down" scenarios fail fast when container is paused
            final var mongoUri = mongoDBContainer.getReplicaSetUrl()
                                 + "?serverSelectionTimeoutMS=3000&socketTimeoutMS=3000&connectTimeoutMS=3000";
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    "spring.mongodb.uri=" + mongoUri);
        }
    }
}
