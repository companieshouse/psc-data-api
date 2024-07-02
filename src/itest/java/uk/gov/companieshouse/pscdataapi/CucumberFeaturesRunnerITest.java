package uk.gov.companieshouse.pscdataapi;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.companieshouse.api.api.CompanyMetricsApiService;
import uk.gov.companieshouse.pscdataapi.api.ChsKafkaApiService;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "uk.gov.companieshouse.pscdataapi")
public class CucumberFeaturesRunnerITest {

    @MockBean
    public ChsKafkaApiService chsKafkaApiService;

    @MockBean
    public CompanyMetricsApiService companyMetricsApiService;
}