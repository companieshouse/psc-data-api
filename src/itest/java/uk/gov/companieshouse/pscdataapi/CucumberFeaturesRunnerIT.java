package uk.gov.companieshouse.pscdataapi;

import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscdataapi.service.CompanyExemptionsApiService;
import uk.gov.companieshouse.pscdataapi.service.CompanyMetricsApiService;

@Suite
@SelectClasspathResource("features")
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class CucumberFeaturesRunnerIT {

    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:7.0.16"));

    @MockitoBean
    private ChsKafkaApiService chsKafkaApiService;

    @MockitoBean
    private CompanyMetricsApiService companyMetricsApiService;

    @MockitoBean
    private CompanyExemptionsApiService companyExemptionsApiService;

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("feature.psc_individual_full_record_get", () -> true);
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        mongoDBContainer.start();
    }


}