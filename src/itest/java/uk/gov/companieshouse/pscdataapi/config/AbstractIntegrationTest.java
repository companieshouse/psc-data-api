package uk.gov.companieshouse.pscdataapi.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.api.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.api.api.CompanyMetricsApiService;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;

/**
 * Loads the application context.
 * Best place to mock your downstream calls.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles({"test"})
public abstract class AbstractIntegrationTest extends AbstractMongoConfig {

    @MockBean
    public ChsKafkaApiService chsKafkaApiService;

    @MockBean
    public CompanyMetricsApiService companyMetricsApiService;

    @MockBean
    CompanyExemptionsApiService companyExemptionsApiService;

}