package uk.gov.companieshouse.pscdataapi.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.api.api.CompanyMetricsApiService;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;

/**
 * Loads the application context.
 * Best place to mock your downstream calls.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CucumberContextConfiguration
@AutoConfigureMockMvc
@DirtiesContext
@ActiveProfiles({"test"})
public class AbstractIntegrationTest extends AbstractMongoConfig {

    @MockBean
    public ChsKafkaApiService chsKafkaApiService;

    @MockBean
    public CompanyMetricsApiService companyMetricsApiService;

}